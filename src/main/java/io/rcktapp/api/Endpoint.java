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
package io.rcktapp.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Endpoint extends Rule
{
   List<Action> actions = new ArrayList();

   public boolean matches(String method, String path)
   {
      if (methods.contains(method))
      {
         for (String includePath : includePaths)
         {
            if (pathMatches(includePath, path))
            {
               for (String excludePath : excludePaths)
               {
                  if (pathMatches(excludePath, path))
                  {
                     return false;
                  }
               }

               return true;
            }
         }
      }
      return false;
   }

   public void addHandler(Handler handler)
   {
      Action a = new Action();
      a.setHandler(handler);
      addAction(a);
   }

   public List<Action> getActions(Request req)
   {
      List<Action> filtered = new ArrayList();
      for (Action a : actions)
      {
         if (a.matches(req.getMethod(), req.getPath()))
            filtered.add(a);
      }

      Collections.sort(filtered);
      //Collections.reverse(filtered);
      return filtered;
   }

   public void setActions(List<Action> actions)
   {
      this.actions.clear();
      for (Action action : actions)
         addAction(action);
   }

   public void addAction(Action action)
   {
      if (!actions.contains(action))
         actions.add(action);

      Collections.sort(actions);
   }

}