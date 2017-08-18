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
package com.generallycloud.baseio.component;

/**
 * @author wangkai
 *
 */
public class DoubleLinkableGroup {

    private DoubleLinkable rootLink;

    private DoubleLinkable tailLink;

    public DoubleLinkable getRootLink() {
        return rootLink;
    }

    public void addLink(DoubleLinkable linkable) {

        if (rootLink == null) {
            rootLink = linkable;
            tailLink = rootLink;
            return;
        }

        tailLink.setNext(linkable);

        tailLink = linkable;
    }

    public void removeLink(DoubleLinkable linkable) {
        //TODO removeLink
        throw new UnsupportedOperationException();
    }

    public void clear() {
        rootLink = null;
        tailLink = null;
    }

}
