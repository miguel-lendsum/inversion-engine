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
package io.rcktapp.rql.elasticsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Very basic Elastic sorting.  Using a class for this since it will eventually be fleshed-out.
 * Obviously, nested-sorting is not handled atm.
 * @author kfrankic
 *
 */
public class Order
{
   @JsonIgnore
   private List<Map<String, String>> orderList;
   
   public Order(String name, String order) {
      orderList = new ArrayList<Map<String, String>>();
      addOrder(name, order);
   }
   
   public void addOrder(String name, String order) {
      orderList.add(Collections.singletonMap(name, order));
   }
   
   /**
    * @return the orderList
    */
   public List<Map<String, String>> getOrderList()
   {
      return orderList;
   }
   
}