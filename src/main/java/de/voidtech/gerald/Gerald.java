/*
	BaristaGerald A General Purpose Discord Bot
    Copyright (C) 2020-2025  Barista Gerald Dev Team (https://github.com/Gerald-Development/Barista-Gerald)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package main.java.de.voidtech.gerald;

import main.java.de.voidtech.gerald.service.GeraldConfigService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class Gerald {
    public static void main(String[] args) {
        SpringApplication springApp = new SpringApplication(Gerald.class);

        GeraldConfigService configService = new GeraldConfigService();
        Properties properties = new Properties();

        properties.put("spring.datasource.url", configService.getConnectionURL());
        properties.put("spring.datasource.username", configService.getDBUser());
        properties.put("spring.datasource.password", configService.getDBPassword());
        properties.put("spring.jpa.properties.hibernate.dialect", configService.getHibernateDialect());
        properties.put("jdbc.driver", configService.getDriver());
        properties.put("spring.jpa.hibernate.ddl-auto", "update");
        properties.put("spring.jpa.hibernate.naming.physical-strategy", "main.java.de.voidtech.gerald.persistence.CustomPhysicalNamingStrategy");

        springApp.setDefaultProperties(properties);
        springApp.run(args);
    }
}