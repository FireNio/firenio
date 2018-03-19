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

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

import com.generallycloud.baseio.component.SocketSelectorEventLoop.SelectionKeySet;

/**
 * @author wangkai
 *
 */
public class SelectionKeyNioSocketSelector extends NioSocketSelector {

    private SelectionKeySet selectionKeySet;

    SelectionKeyNioSocketSelector(SocketSelectorEventLoop selectorEventLoop,
            SelectableChannel channel, Selector selector, SelectionKeySet selectionKeySet) {
        super(selectorEventLoop, channel, selector);
        this.selectionKeySet = selectionKeySet;
    }

    @Override
    public int select() throws IOException {
        selectionKeySet.reset();
        return selector.select();
    }

    @Override
    public int select(long timeout) throws IOException {
        selectionKeySet.reset();
        return selector.select(timeout);
    }

    @Override
    public Set<SelectionKey> selectedKeys() throws IOException {
        return selectionKeySet;
    }

    @Override
    public int selectNow() throws IOException {
        selectionKeySet.reset();
        return selector.selectNow();
    }

}
