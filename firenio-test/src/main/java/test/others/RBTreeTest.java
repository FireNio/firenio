package test.others;

/*
 * Copyright 2015 The FireNio Project
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
public class RBTreeTest {

  public static void main(String[] args) {

      RBTree<String> tree = new RBTree<>();

      tree.insert(5, "5");
      tree.insert(2, "2");
      tree.insert(1, "1");
      tree.insert(2, "2");
      tree.insert(4, "4");
      tree.insert(6, "6");

      for(;;){
          RBTree.RBTNode n = tree.maximum();
          if (n == null){
              break;
          }
          System.out.println(n.value);
          tree.remove(n);
      }




  }

}