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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.rocketpartners.cloud.api;

import java.sql.Timestamp;

public class Dto
{
   protected long id        = 0;

   long           accountId = 0;

   @Override
   public boolean equals(Object o)
   {
      if (o == null)
         return false;

      if (id > 0)
         return o.getClass().equals(getClass()) && id == ((Dto) o).id;
      else
         return o == this;
   }

   @Override
   public int hashCode()
   {
      return toString().hashCode();
   }

   @Override
   public String toString()
   {
      if (id == 0)
      {
         return (getClass().getSimpleName() + "(0," + System.identityHashCode(this) + ")");
      }
      return (getClass().getSimpleName() + "(" + id + ")");
   }

   public final long getId()
   {
      return id;
   }

   public final void setId(long id)
   {
      this.id = id;
   }

   public long getAccountId()
   {
      return accountId;
   }

   public void setAccountId(long orgId)
   {
      this.accountId = orgId;
   }

}