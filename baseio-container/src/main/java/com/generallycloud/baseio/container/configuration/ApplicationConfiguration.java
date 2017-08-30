/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.container.configuration;

public class ApplicationConfiguration {

    private boolean               APP_ENABLE_REDEPLOY   = true;
    
    private boolean               APP_ENABLE_STOPSERVER = true;

    private FiltersConfiguration  filtersConfiguration;

    private PluginsConfiguration  pluginsConfiguration;

    private ServicesConfiguration servletsConfiguration;

    public FiltersConfiguration getFiltersConfiguration() {
        return filtersConfiguration;
    }

    public PluginsConfiguration getPluginsConfiguration() {
        return pluginsConfiguration;
    }

    public ServicesConfiguration getServletsConfiguration() {
        return servletsConfiguration;
    }

    protected void setFiltersConfiguration(FiltersConfiguration filtersConfiguration) {
        this.filtersConfiguration = filtersConfiguration;
    }

    protected void setPluginsConfiguration(PluginsConfiguration pluginsConfiguration) {
        this.pluginsConfiguration = pluginsConfiguration;
    }

    protected void setServletsConfiguration(ServicesConfiguration servletsConfiguration) {
        this.servletsConfiguration = servletsConfiguration;
    }

    public boolean isAPP_ENABLE_REDEPLOY() {
        return APP_ENABLE_REDEPLOY;
    }

    public void setAPP_ENABLE_REDEPLOY(boolean APP_ENABLE_REDEPLOY) {
        this.APP_ENABLE_REDEPLOY = APP_ENABLE_REDEPLOY;
    }

    public boolean isAPP_ENABLE_STOPSERVER() {
        return APP_ENABLE_STOPSERVER;
    }

    public void setAPP_ENABLE_STOPSERVER(boolean APP_ENABLE_STOPSERVER) {
        this.APP_ENABLE_STOPSERVER = APP_ENABLE_STOPSERVER;
    }

    

}
