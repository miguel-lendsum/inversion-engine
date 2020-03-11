package io.inversion.cloud.elastic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.h2.table.Table;

import io.inversion.cloud.action.elastic.ElasticsearchDb;
import io.inversion.cloud.action.rest.RestAction;
import io.inversion.cloud.jdbc.JdbcDbFactory;
import io.inversion.cloud.model.Api;
import io.inversion.cloud.model.Collection;
import io.inversion.cloud.model.JSArray;
import io.inversion.cloud.model.JSNode;
import io.inversion.cloud.model.Response;
import io.inversion.cloud.service.Engine;

/**
 * @author tc-rocket
 *
 */
public class ElasticsearchDbFactory
{

   public static void main(String[] args) throws Exception
   {
      buildNorthwindElasticsearchDb();
   }

   protected static ElasticsearchDb buildNorthwindElasticsearchDb() throws Exception
   {
      rebuildNorthwind();
      return new NorthwindElasticsearchDb();
   }

   public static class NorthwindElasticsearchDb extends ElasticsearchDb implements Serializable
   {
      public NorthwindElasticsearchDb()
      {
         super("northwind");
      }

      @Override
      public void configApi(Api api)
      {
         Collection northwind = getCollection("northwind");
         removeCollection(northwind);

         Collection orders = northwind.copy().withName("orders");
         withCollection(orders);

         super.configApi(api);
      }
   }

   protected static void rebuildNorthwind() throws Exception
   {
      try
      {
        // AmazonDynamoDB client = DynamoDb.buildDynamoClient("dynamo");
         //DeleteTableRequest dtr = new DeleteTableRequest().withTableName("northwind");
         //client.deleteTable(dtr);
      }
      catch (Exception ex)
      {
         //ex.printStackTrace();
      }

      Api h2Api = new Api("northwind");
      h2Api.withDb(JdbcDbFactory.bootstrapH2("dynamodbtesting"));
      h2Api.withEndpoint("*", "/*", new RestAction());
      Engine h2Engine = new Engine().withApi(h2Api);

      Engine elasticEngine = new Engine().withApi(new Api("northwind")//
                                                                     .withDb(new NorthwindElasticsearchDb())//
                                                                     .withEndpoint("*", "/*", new RestAction()));

      System.out.println("");
      System.out.println("RELOADING ELASTIC SEARCH...");

      Response res = null;
      int pages = 0;
      int total = 0;
      String start = "northwind/orders?pageSize=100&sort=orderid";
      String next = start;
      do
      {
         JSArray toPost = new JSArray();

         res = h2Engine.get(next);
         res.assertOk();
         if (res.getData().size() == 0)
            break;

         pages += 1;
         next = res.next();

         //-- now post to Elastic
         for (Object o : res.getData())
         {
            total += 1;
            JSNode js = (JSNode) o;

            js.remove("href");
            js.put("type", "ORDER");

            for (String key : js.keySet())
            {
               String value = js.getString(key);
               if (value != null && (value.startsWith("http://") || value.startsWith("https://")))
               {
                  value = value.substring(value.lastIndexOf("/") + 1, value.length());
                  js.remove(key);

                  if (!key.toLowerCase().endsWith("id"))
                     key = key + "Id";

                  js.put(key, value);
               }
            }
            toPost.add(js);
         }

         res = elasticEngine.post("northwind/orders", toPost);
         res.dump();
         assertEquals(201, res.getStatusCode());
         System.out.println("ELASTIC SEARCH LOADED: " + total);// + " - " + js.getString("orderid"));
      }
      while (pages < 200 && next != null);

   }

}
