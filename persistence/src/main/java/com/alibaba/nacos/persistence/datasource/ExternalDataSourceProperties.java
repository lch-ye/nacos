/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.persistence.datasource;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.Preconditions;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.utils.Sm4Util;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.alibaba.nacos.common.utils.CollectionUtils.getOrDefault;

/**
 * Properties of external DataSource.
 *
 * @author Nacos
 */
public class ExternalDataSourceProperties {
    
    private static final String JDBC_DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    
    private static final String TEST_QUERY = "SELECT 1";
    
    private Integer num;
    
    private List<String> url = new ArrayList<>();
    
    private List<String> user = new ArrayList<>();
    
    private List<String> password = new ArrayList<>();

    private List<String> sm4Key = new ArrayList<>();


    public void setNum(Integer num) {
        this.num = num;
    }
    
    public void setUrl(List<String> url) {
        this.url = url;
    }
    
    public void setUser(List<String> user) {
        this.user = user;
    }
    
    public void setPassword(List<String> password) {
        this.password = password;
    }

    public void setSm4Key(List<String> sm4Key) {
        this.sm4Key = sm4Key;
    }

    /**
     * Build serveral HikariDataSource.
     *
     * @param environment {@link Environment}
     * @param callback    Callback function when constructing data source
     * @return List of {@link HikariDataSource}
     */
    List<HikariDataSource> build(Environment environment, Callback<HikariDataSource> callback) {
        List<HikariDataSource> dataSources = new ArrayList<>();
        Binder.get(environment).bind("db", Bindable.ofInstance(this));
        Preconditions.checkArgument(Objects.nonNull(num), "db.num is null");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(user), "db.user or db.user.[index] is null");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(password), "db.password or db.password.[index] is null");
        for (int index = 0; index < num; index++) {
            int currentSize = index + 1;
            Preconditions.checkArgument(url.size() >= currentSize, "db.url.%s is null", index);
            DataSourcePoolProperties poolProperties = DataSourcePoolProperties.build(environment);
            if (StringUtils.isEmpty(poolProperties.getDataSource().getDriverClassName())) {
                poolProperties.setDriverClassName(JDBC_DRIVER_NAME);
            }
            poolProperties.setJdbcUrl(url.get(index).trim());

            if(sm4Key.size() == 0){
                sm4Key.add("91C43180D2844ED1F12B859DR5012151");
            }
            String split = "DECRYPT@";
            String key = getOrDefault(sm4Key, index, sm4Key.get(0)).trim();
            String reallyUserName = "";
            String reallyPassword = "";
            try {
                String userNameEn = getOrDefault(user, index, user.get(0)).trim();
                String passwordEn = getOrDefault(password, index, password.get(0)).trim();

                if(userNameEn.startsWith(split)){
                    userNameEn = userNameEn.replace(split, "");
                    //解密用户名
                    reallyUserName = Sm4Util.decryptEcb(key, userNameEn);
                } else {
                    reallyUserName = userNameEn;
                }

                if(passwordEn.startsWith(split)){
                    passwordEn = passwordEn.replace(split, "");
                    //解密密码
                    reallyPassword = Sm4Util.decryptEcb(key, passwordEn);
                } else {
                    reallyPassword = passwordEn;
                }
            } catch (Exception e){

            }
            poolProperties.setUsername(reallyUserName);
            poolProperties.setPassword(reallyPassword);

            HikariDataSource ds = poolProperties.getDataSource();
            if (StringUtils.isEmpty(ds.getConnectionTestQuery())) {
                ds.setConnectionTestQuery(TEST_QUERY);
            }
            
            dataSources.add(ds);
            callback.accept(ds);
        }
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(dataSources), "no datasource available");
        return dataSources;
    }
    
    interface Callback<D> {
        
        /**
         * Perform custom logic.
         *
         * @param datasource dataSource.
         */
        void accept(D datasource);
    }
}
