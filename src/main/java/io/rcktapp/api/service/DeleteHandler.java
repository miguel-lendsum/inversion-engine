/*
 * Copyright (c) 2015-2018 Rocket Partners, LLC
 * http://rocketpartners.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.rcktapp.api.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.forty11.j.J;
import io.forty11.sql.Sql;
import io.forty11.web.js.JSArray;
import io.forty11.web.js.JSObject;
import io.rcktapp.api.Action;
import io.rcktapp.api.Api;
import io.rcktapp.api.ApiException;
import io.rcktapp.api.Chain;
import io.rcktapp.api.Collection;
import io.rcktapp.api.Db;
import io.rcktapp.api.Endpoint;
import io.rcktapp.api.Entity;
import io.rcktapp.api.Request;
import io.rcktapp.api.Response;
import io.rcktapp.api.SC;
import io.rcktapp.rql.RQL;
import io.rcktapp.rql.Replacer;
import io.rcktapp.rql.Stmt;

public class DeleteHandler extends RqlHandler
{
   boolean      allowBatchDelete = true;

   List<String> batchAllow       = new ArrayList();
   List<String> batchDeny        = new ArrayList();

   @Override
   public void service(Service service, Api api, Endpoint endpoint, Action action, Chain chain, Request req, Response res) throws Exception
   {
      String entityKey = req.getEntityKey();

      if (req.getJson() != null)
      {
         if (!J.empty(entityKey))
            throw new ApiException(SC.SC_400_BAD_REQUEST, "You can't DELETE to an entity key in the url and also include a JSON body.");

         JSObject obj = req.getJson();
         if (!(obj instanceof JSArray))
         {
            throw new ApiException(SC.SC_400_BAD_REQUEST, "The JSON body to a DELETE must be an array that contains string urls.");
         }

         List<String> urls = new ArrayList();

         for (Object o : ((JSArray) obj).asList())
         {
            if (!(o instanceof String))
               throw new ApiException(SC.SC_400_BAD_REQUEST, "The JSON body to a DELETE must be an array that contains string urls.");

            String url = (String) o;

            String path = req.getUrl().toString();
            if (path.indexOf("?") > 0)
               path = path.substring(0, path.indexOf("?") - 1);

            if (!url.toLowerCase().startsWith(path.toLowerCase()))
            {
               throw new ApiException(SC.SC_400_BAD_REQUEST, "All delete request must be for the collection in the original request: '" + path + "'");
            }
            urls.add((String) o);
         }

         List<String> deletedKeys = new ArrayList<>();

         for (String url : urls)
         {
            Response r = chain.getService().include(chain, "DELETE", url, null);
            if (r.getStatusCode() != 200)
            {
               throw new ApiException("Nested delete url: " + url + " failed!");
            }
            JSArray keys = r.getJson().getArray("data");
            if (keys != null)
               deletedKeys.addAll(keys.asList());
         }
         JSObject deletedKeyJs = new JSObject();
         deletedKeyJs.put("deletedKeys", new JSArray(deletedKeys));

         res.setJson(deletedKeyJs);
      }
      else
      {
         String collection = req.getCollectionKey().toLowerCase();

         if (!J.empty(entityKey) || //
               (allowBatchDelete && //
                     ((batchAllow.isEmpty() && batchDeny.isEmpty()) || //
                           (!batchAllow.isEmpty() && batchAllow.contains(collection)) || (batchAllow.isEmpty() && !batchDeny.contains(collection)))))
         {
            delete(chain, req, res);
         }
         else
         {
            throw new ApiException(SC.SC_400_BAD_REQUEST, "Batch deletes are not allowed for this collection");
         }
      }
   }

   void delete(Chain chain, Request req, Response res) throws Exception
   {
      try
      {
         Db db = chain.getService().getDb(req.getApi(), req.getCollectionKey());
         Connection conn = chain.getService().getConnection(db);

         RQL rql = makeRql(chain);

         Collection collection = req.getApi().getCollection(req.getCollectionKey());

         Entity entity = collection.getEntity();
         String entityKey = req.getEntityKey();

         //String table = rql.asCol(entity.getTable().getName());

         Map params = req.getParams();
         String keyAttr = collection.getEntity().getKey().getName();
         if (!J.empty(entityKey))
         {
            params.put("in(`" + keyAttr + "`," + entityKey + ")", null);
         }

         String sql = "SELECT " + rql.asCol(collection.getEntity().getKey().getColumn().getName()) + " FROM " + rql.asCol(entity.getTable().getName());

         Replacer replacer = new Replacer();
         Stmt stmt = rql.toSql(sql, entity.getTable(), params, replacer);
         sql = stmt.toSql();

         if (sql.toLowerCase().indexOf(" where ") < 0)
            throw new ApiException(SC.SC_400_BAD_REQUEST, "You can't delete from a table without a where clause or an individual ID.");

         List args = new ArrayList();
         for (int i = 0; i < replacer.cols.size(); i++)
         {
            String col = replacer.cols.get(i);
            String val = replacer.vals.get(i);

            args.add(cast(collection, col, val));
         }

         List<Long> idsToDelete = Sql.selectList(conn, sql, args);

         if (!idsToDelete.isEmpty())
            Sql.execute(conn, "DELETE FROM " + rql.asCol(entity.getTable().getName()) + " WHERE " + rql.asCol(collection.getEntity().getKey().getColumn().getName()) + " IN (" + Sql.getQuestionMarkStr(idsToDelete.size()) + ")", idsToDelete);

         JSObject resJson = new JSObject();
         resJson.put("data", new JSArray(idsToDelete));
         res.setJson(resJson);

         for (Long id : idsToDelete)
         {
            res.addChange("DELETE", collection.getName(), Long.toString(id));
         }
      }
      catch (Exception e)
      {
         throw new ApiException(SC.SC_500_INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }

   }
}