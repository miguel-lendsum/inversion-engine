/*
 * Copyright (c) 2015-2019 Rocket Partners, LLC
 * https://github.com/inversion-api
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
package io.inversion.cloud.demo.demo001;

import io.inversion.cloud.action.rest.RestAction;
import io.inversion.cloud.action.sql.SqlDb;
import io.inversion.cloud.model.Api;
import io.inversion.cloud.service.spring.InversionApp;
import io.inversion.cloud.utils.Utils;

/**
 * This demo launches an API that exposes SQL database tables as REST collection endpoints.  
 * The demo supports full GET,PUT,POST,DELETE operations with an extensive Resource Query Language
 * (RQL) for GET requests
 *   
 * @see https://github.com/inversion-api/inversion-engine for more information on building awesome APIs with Inversion 
 * @author wellsb1
 *
 */
public class Demo001SqlDbNorthwind
{
   /**
    * This simple factory method is static so that other  
    * demos can use and extend this api configuration.
    */
   public static Api buildApi()
   {
      return new Api()//
                      .withName("northwind")//

                      //-- DATABASE CONFIGURATION OPTION #1.
                      //-- you can set your database connection information explicitly in the code here... 
                      .withDb(new SqlDb("northwind", //the database name used as the properties key prefix when 
                                        "org.h2.Driver", //-- jdbc driver
                                        "jdbc:h2:mem:northwind;DB_CLOSE_DELAY=-1", //-- jdbc url 
                                        "sa", //-- jdbc user
                                        "", //jdbc password
                                        //OPTIONAL: the demo db is an in-memory db that gets
                                        //reinitialized each time with the data in "northwind-h2.ddl"
                                        SqlDb.class.getResource("northwind-h2.ddl").toString()))

                      //-- DATABASE CONFIGURATION OPTION #2 & #3
                      //-- comment out the above  "withDb()" method and uncomment below
                      //.withDb(new SqlDb("northwind"))

                      //-- then add the following name value pairs to one of the following
                      //--   - to an 'inversion.properties' file in the classpath
                      //--   - OR as java system properties
                      //--   - OR as environment variables
                      //-- 
                      //--  northwind.driver=${YOUR_JDBC_DRIVER}
                      //--  northwind.url=${YOUR_JDBC_URL}
                      //--  northwind.user=${YOUR_JDBC_USERNAME}
                      //--  northwind.pass=${YOUR_JDBC_PASSWORD}

                      //-- the RestAction performs CRUD operations on Db objects.
                      .withEndpoint("GET,PUT,POST,DELETE", "/*", new RestAction());

   }

   public static void main(String[] args) throws Exception
   {
      //Launches the API on port 8080
      InversionApp.run(buildApi());
      
      //this simply prints out the README to the console  
      System.out.println(Utils.read(Demo001SqlDbNorthwind.class.getResourceAsStream("README.md")));
   }


}
