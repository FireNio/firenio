/*
 * Copyright 2015 The Baseio Project
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
package test.others.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.alibaba.fastjson.JSON;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.log.DebugUtil;

public class LuceneDemo {

    public static List<Item> searchIndexer(Analyzer analyzer, IndexSearcher searcher,
            String keyword) {
        List<Item> result = new ArrayList<Item>();
        try {
            // 对多field进行搜索
            String[] multiFields = new String[] { "id", "title", "content" };
            MultiFieldQueryParser parser = new MultiFieldQueryParser(multiFields, analyzer);
            // 设定具体的搜索词
            Query query = parser.parse(keyword);
            ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = searcher.doc(hits[i].doc);
                Item item = new Item();
                item.setId(hitDoc.get("id"));
                item.setContent(hitDoc.get("content"));
                result.add(item);
            }
        } catch (Exception e) {
            DebugUtil.error(e.getMessage(), e);
            return null;
        }
        return result;
    }

    static void createIndex(IndexWriter writer) throws IOException {
        writer.deleteAll();
        List<Item> items = new ArrayList<Item>();
        items.add(new Item("1", "This is the text to be greatly indexed."));
        items.add(new Item("2", "This is great."));
        items.add(new Item("3", "I love apple and pear. "));
        items.add(new Item("4", "I am chinese."));
        items.add(new Item("5", "My name is lichade."));
        items.add(new Item("6", "Are you chinese?"));
        try {
            // 将文档信息存入索引
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                Document doc = new Document();
                doc.add(new Field("id", item.getId(), TextField.TYPE_STORED));
                doc.add(new Field("content", item.getContent(), TextField.TYPE_STORED));
                writer.addDocument(doc);
            }
        } catch (Exception e) {
            DebugUtil.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        // 索引存储到硬盘
        File file = new File("c:/temp/lucene");
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(file.toPath());
        // create index
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));
        createIndex(writer);
        // search index
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        List<Item> result = searchIndexer(analyzer, searcher, "chinese");
        for (Item item : result) {
            System.out.println(item.toString());
        }
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^");
        result = searchIndexer(analyzer, searcher, "greatly");
        for (Item item : result) {
            System.out.println(item.toString());
        }
        Util.close(writer);
        Util.close(reader);
        Util.close(directory);
        Util.close(analyzer);
    }

    static class Item {

        private String id;
        private String content;

        public Item() {}

        public Item(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String toString() {
            return JSON.toJSONString(this);
        }

    }
}
