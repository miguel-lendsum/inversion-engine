/*
 * Copyright (c) 2016-2019 Rocket Partners, LLC
 * http://rocketpartners.io
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rocketpartners.cloud.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tc-rocket
 *
 */
public class Index
{
   protected Table        table   = null;
   protected String       name    = null;
   protected String       type    = null;           // primary, partition, sort, localsecondary, etc
   protected List<Column> columns = new ArrayList();

   public Index()
   {
      super();
   }

   public Index(Table table, String name, String type)
   {
      super();
      withTable(table);
      withName(name);
      withType(type);
   }

   public Table getTable()
   {
      return table;
   }

   public Index withTable(Table table)
   {
      if (this.table != table)
      {
         this.table = table;
         table.addIndex(this);
      }
      return this;
   }

   public String getName()
   {
      return name;
   }

   public Index withName(String name)
   {
      this.name = name;
      return this;
   }

   public String getType()
   {
      return type;
   }

   public Index withType(String type)
   {
      this.type = type;
      return this;
   }

   public boolean isType(String... types)
   {
      for (String type : types)
      {
         if (type.equalsIgnoreCase(this.type))
            return true;
      }
      return false;
   }

   public boolean hasColumn(String name)
   {
      for (Column col : columns)
      {
         if (col.getName().equalsIgnoreCase(name))
            return true;
      }
      return false;
   }

   public List<Column> getColumns()
   {
      return new ArrayList(columns);
   }

   public void setColumns(List<Column> columns)
   {
      this.columns.clear();
      for (Column column : columns)
      {
         withColumn(column);
      }
   }

   public Index withColumn(Column column)
   {
      if (column != null && !columns.contains(column))
         columns.add(column);

      return this;
   }

   public void removeColumn(Column column)
   {
      columns.remove(column);
   }

   public String toString()
   {
      return name == null ? super.toString() : name + " " + columns;
   }

}