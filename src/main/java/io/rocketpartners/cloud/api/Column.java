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

public class Column extends Dto implements Comparable<Column>
{
   Table   table    = null;

   /**
    *  If this Col is a foreign key, this will be populated
    *  with the refrenced primary key from the referred table
    */
   Column  pk       = null;

   int     number   = 0;
   String  name     = null;
   String  type     = null;
   boolean nullable = false;
   boolean unique   = false;
   boolean exclude  = false;

   String  hint     = null;

   public Column(Table table, int number, String name, String type, boolean nullable)
   {
      super();
      this.table = table;
      this.number = number;
      this.name = name;
      this.type = type;
      this.nullable = nullable;
      this.hint = table.getName() + "." + name;
   }

   @Override
   public int compareTo(Column o)
   {
      if (o == null)
         return 1;

      if (o.table == table)
         return this.number > o.number ? 1 : -1;

      return 0;
   }

   public boolean isExclude()
   {
      return exclude;
   }

   public void setExclude(boolean exclude)
   {
      this.exclude = exclude;
   }

   public String toString()
   {
      return hint == null ? super.toString() : hint;
   }

   /**
    * @return the primaryKey
    */
   public Column getPk()
   {
      return pk;
   }

   /**
    * @param primaryKey the primaryKey to set
    */
   public void setPk(Column pk)
   {
      this.pk = pk;
   }

   public boolean isFk()
   {
      return pk != null;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return the type
    */
   public String getType()
   {
      return type;
   }

   /**
    * @param type the type to set
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * @return the tbl
    */
   public Table getTable()
   {
      return table;
   }

   /**
    * @param tbl the tbl to set
    */
   public void setTable(Table tbl)
   {
      this.table = tbl;
   }

   /**
    * @return the hint
    */
   public String getHint()
   {
      return hint;
   }

   /**
    * @param hint the hint to set
    */
   public void setHint(String hint)
   {
      this.hint = hint;
   }

   /**
    * @return the unique
    */
   public boolean isUnique()
   {
      return unique;
   }

   /**
    * @param unique the unique to set
    */
   public void setUnique(boolean unique)
   {
      this.unique = unique;
   }

   public boolean isNullable()
   {
      return nullable;
   }

   public void setNullable(boolean nullable)
   {
      this.nullable = nullable;
   }

}