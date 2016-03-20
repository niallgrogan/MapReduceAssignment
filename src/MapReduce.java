/*
 * Niall Grogan - 12429338
 * Stephen Dooley - 12502947
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapReduce {

    public static void main(String[] args) {

        // get number of files to be reduced from the user
        Scanner reader = new Scanner(System.in);
        //System.out.println("Enter the number of files in database:");
        //int poolSize = reader.nextInt();
        int poolSize = 3;

        //PART 1
        // create an input with the name of a file and a String
        // containing every word in the file
        Map<String, String> input = new HashMap<String, String>();
        // get names of the files to search from the program arguments
        for(String fileName:args) {
            String everything = "";
            try {
                // create a buffered reader and read each line of the file
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                // for concatenating multiple strings in a loop
                // much faster than String object in loop concatenation
                StringBuilder sb = new StringBuilder();
                // read a line from the file
                String line = br.readLine();

                // if the line of the file contains text
                while (line != null) {
                    // add the line of the text to the string builder
                    sb.append(line);
                    // add line separator after each line
                    // this ensures words at the beginning and end of new lines
                    // do not concatenate as one
                    sb.append(System.lineSeparator());
                    // read the next line of the file
                    line = br.readLine();
                }
                // convert the string builder to a String containing
                // the entire file
                everything = sb.toString();
                br.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            // add the words to the Hash map
            // contains 3 entries, one for each file
            input.put(fileName,everything);
        }


       // APPROACH #1: Brute force
       {
           // create a map containing String and another map.
           // inner map contains a String and in
           //
           // MAP ( String, MAP(String, int) )
           //
           Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();

           Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
           // while the input contains another entry
           // (should contain 3 entries -- one for each file)
           while(inputIter.hasNext()) {
               // get the file name and the string containing all words
               Map.Entry<String, String> entry = inputIter.next();
               // get the key (filename)
               String file = entry.getKey();
               // get the value (words)
               String contents = entry.getValue();
               // split the string containing the words into an
               // array of words
               String[] words = contents.trim().split("\\s+");

               // LOOP THROUGH EACH WORD FOR A GIVEN FILE
               for(String word : words) {
                   // files = {file1.txt=1,
                   // get the file and number of occurrences for a given word
                   Map<String, Integer> files = output.get(word);
                   // one FIRST iteration, the output map will NOT contain any entries
                   // ==> file will be null
                   if (files == null) {
                       // create map to assign it as the value of the output map
                       files = new HashMap<String, Integer>();
                       // this map consists of the of the word, and a map of files and occurrences
                       output.put(word, files);
                   }
                   // remove the file name from the map of file names and occurrences
                   Integer occurrences = files.remove(file);
                   if (occurrences == null) {
                       // put the name of the file and the number of occurrences into the files map
                       files.put(file, 1);
                   } else {
                       // put the name of the file and the number of occurrences into the files map
                       files.put(file, occurrences.intValue() + 1);
                   }
               }
           }

           // show me:
//            System.out.println("\nApproach 1:");
//            System.out.println(output);
       }

        // APPROACH #1: Brute force
        {
            // create a map containing String and another map.
            // inner map contains a String and in
            //
            // MAP ( String, MAP(String, int) )
            //
            Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();

            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            // while the input contains another entry
            // (should contain 3 entries -- one for each file)
            while(inputIter.hasNext()) {
                // get the file name and the string containing all words
                Map.Entry<String, String> entry = inputIter.next();
                // get the key (filename)
                String file = entry.getKey();
                // get the value (words)
                String contents = entry.getValue();
                // split the string containing the words into an
                // array of words
                String[] words = contents.trim().split("\\s+");

                // LOOP THROUGH EACH WORD FOR A GIVEN FILE
                for(String word : words) {
                    // files = {file1.txt=1,
                    // get the file and number of occurrences for a given word
                    Map<String, Integer> files = output.get(word);
                    // one FIRST iteration, the output map will NOT contain any entries
                    // ==> file will be null
                    if (files == null) {
                        // create map to assign it as the value of the output map
                        files = new HashMap<String, Integer>();
                        // this map consists of the of the word, and a map of files and occurrences
                        output.put(word, files);
                    }
                    // remove the file name from the map of file names and occurrences
                    Integer occurrences = files.remove(file);
                    if (occurrences == null) {
                        // put the name of the file and the number of occurrences into the files map
                        files.put(file, 1);
                    } else {
                        // put the name of the file and the number of occurrences into the files map
                        files.put(file, occurrences.intValue() + 1);
                    }
                }
            }

              // show me:
//            System.out.println("\nApproach 1:");
//            System.out.println(output);
        }

//
//        // APPROACH #2: MapReduce
//        {
//            Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();
//
//            // MAP:
//
//            List<MappedItem> mappedItems = new LinkedList<MappedItem>();
//
//            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
//            while(inputIter.hasNext()) {
//                Map.Entry<String, String> entry = inputIter.next();
//                String file = entry.getKey();
//                String contents = entry.getValue();
//
//                map(file, contents, mappedItems);
//            }
//
//            // GROUP:
//
//            Map<String, List<String>> groupedItems = new HashMap<String, List<String>>();
//
//            Iterator<MappedItem> mappedIter = mappedItems.iterator();
//            while(mappedIter.hasNext()) {
//                MappedItem item = mappedIter.next();
//                String word = item.getWord();
//                String file = item.getFile();
//                List<String> list = groupedItems.get(word);
//                if (list == null) {
//                    list = new LinkedList<String>();
//                    groupedItems.put(word, list);
//                }
//                list.add(file);
//            }
//
//            // REDUCE:
//
//            Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
//            while(groupedIter.hasNext()) {
//                Map.Entry<String, List<String>> entry = groupedIter.next();
//                String word = entry.getKey();
//                List<String> list = entry.getValue();
//
//                reduce(word, list, output);
//            }
//
//            System.out.println("\nApproach 2:");
//            System.out.println(output);
//        }


        // APPROACH #3: Distributed MapReduce
        {
            final Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();

            // MAP:

            final List<MappedItem> mappedItems = new LinkedList<MappedItem>();

            final MapCallback<String, MappedItem> mapCallback = new MapCallback<String, MappedItem>() {
                @Override
                public synchronized void mapDone(String file, List<MappedItem> results) {
                    mappedItems.addAll(results);
                }
            };

            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            ExecutorService mapPool = Executors.newFixedThreadPool(poolSize);
            while(inputIter.hasNext()) {
                Map.Entry<String, String> entry = inputIter.next();
                final String file = entry.getKey();
                final String contents = entry.getValue();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        map(file, contents, mapCallback);
                    }
                });
                //Adding thread to threadpool
                mapPool.execute(t);
            }
            //Ensures all threads complete
            mapPool.shutdown();
            long startTime = System.nanoTime();
            //Not sure if this is good practice
            while (!mapPool.isTerminated()) {}
            long time = System.nanoTime() - startTime;

            System.out.println("Finished all map threads in " + time + "ns");

            // GROUP:

            Map<String, List<String>> groupedItems = new HashMap<String, List<String>>();

            Iterator<MappedItem> mappedIter = mappedItems.iterator();
            while(mappedIter.hasNext()) {
                MappedItem item = mappedIter.next();
                String word = item.getWord();
                String file = item.getFile();
                List<String> list = groupedItems.get(word);
                if (list == null) {
                    list = new LinkedList<String>();
                    groupedItems.put(word, list);
                }
                list.add(file);
            }

            // REDUCE:

            final ReduceCallback<String, String, Integer> reduceCallback = new ReduceCallback<String, String, Integer>() {
                @Override
                public synchronized void reduceDone(String k, Map<String, Integer> v) {
                    output.put(k, v);
                }
            };

            Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
            ExecutorService reducePool = Executors.newFixedThreadPool(poolSize);
            while(groupedIter.hasNext()) {
                Map.Entry<String, List<String>> entry = groupedIter.next();
                final String word = entry.getKey();
                final List<String> list = entry.getValue();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        reduce(word, list, reduceCallback);
                    }
                });
                reducePool.execute(t);
            }

            //Ensures all threads complete
            reducePool.shutdown();
            long startTimeReduce = System.nanoTime();
            //Not sure if this is good practice
            while (!reducePool.isTerminated()) {}
            long timeReduce = System.nanoTime() - startTimeReduce;

            System.out.println("Finished all reduce threads in " + timeReduce + "ns");

            System.out.println("\nApproach 3:");
            System.out.println(output);
        }

        // APPROACH #4: Distributed MapReduce with Concurrent Data Structures
        {
            final Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();

            // MAP:

            final List<MappedItem> mappedItems = new LinkedList<MappedItem>();

            final MapCallback<String, MappedItem> mapCallback = new MapCallback<String, MappedItem>() {
                @Override
                public synchronized void mapDone(String file, List<MappedItem> results) {
                    mappedItems.addAll(results);
                }
            };

            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            ExecutorService mapPool = Executors.newFixedThreadPool(poolSize);
            while(inputIter.hasNext()) {
                Map.Entry<String, String> entry = inputIter.next();
                final String file = entry.getKey();
                final String contents = entry.getValue();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        map(file, contents, mapCallback);
                    }
                });
                //Adding thread to threadpool
                mapPool.execute(t);
            }
            //Ensures all threads complete
            mapPool.shutdown();
            long startTime = System.nanoTime();
            //Not sure if this is good practice
            while (!mapPool.isTerminated()) {}
            long time = System.nanoTime() - startTime;

            System.out.println("Finished all map threads in " + time + "ns");

            // GROUP:

            Map<String, List<String>> groupedItems = new HashMap<String, List<String>>();

            Iterator<MappedItem> mappedIter = mappedItems.iterator();
            while(mappedIter.hasNext()) {
                MappedItem item = mappedIter.next();
                String word = item.getWord();
                String file = item.getFile();
                List<String> list = groupedItems.get(word);
                if (list == null) {
                    list = new LinkedList<String>();
                    groupedItems.put(word, list);
                }
                list.add(file);
            }

            // REDUCE:

            final ReduceCallback<String, String, Integer> reduceCallback = new ReduceCallback<String, String, Integer>() {
                @Override
                public synchronized void reduceDone(String k, Map<String, Integer> v) {
                    output.put(k, v);
                }
            };

            Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
            ExecutorService reducePool = Executors.newFixedThreadPool(poolSize);
            while(groupedIter.hasNext()) {
                Map.Entry<String, List<String>> entry = groupedIter.next();
                final String word = entry.getKey();
                final List<String> list = entry.getValue();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        reduce(word, list, reduceCallback);
                    }
                });
                reducePool.execute(t);
            }

            //Ensures all threads complete
            reducePool.shutdown();
            long startTimeReduce = System.nanoTime();
            //Not sure if this is good practice
            while (!reducePool.isTerminated()) {}
            long timeReduce = System.nanoTime() - startTimeReduce;

            System.out.println("Finished all reduce threads in " + timeReduce + "ns");

            System.out.println("\nApproach 3:");
            System.out.println(output);
        }
    }

    public static void map(String file, String contents, List<MappedItem> mappedItems) {
        String[] words = contents.trim().split("\\s+");
        for(String word: words) {
            mappedItems.add(new MappedItem(word, file));
        }
    }

    public static void reduce(String word, List<String> list, Map<String, Map<String, Integer>> output) {
        Map<String, Integer> reducedList = new HashMap<String, Integer>();
        for(String file: list) {
            Integer occurrences = reducedList.get(file);
            if (occurrences == null) {
                reducedList.put(file, 1);
            } else {
                reducedList.put(file, occurrences.intValue() + 1);
            }
        }
        output.put(word, reducedList);
    }

    public static interface MapCallback<E, V> {

        public void mapDone(E key, List<V> values);
    }

    public static void map(String file, String contents, MapCallback<String, MappedItem> callback) {
        String[] words = contents.trim().split("\\s+");
        List<MappedItem> results = new ArrayList<MappedItem>(words.length);
        for(String word: words) {
            results.add(new MappedItem(word, file));
        }
        callback.mapDone(file, results);
    }

    public static interface ReduceCallback<E, K, V> {

        public void reduceDone(E e, Map<K,V> results);
    }

    public static void reduce(String word, List<String> list, ReduceCallback<String, String, Integer> callback) {

        Map<String, Integer> reducedList = new HashMap<String, Integer>();
        for(String file: list) {
            Integer occurrences = reducedList.get(file);
            if (occurrences == null) {
                reducedList.put(file, 1);
            } else {
                reducedList.put(file, occurrences.intValue() + 1);
            }
        }
        callback.reduceDone(word, reducedList);
    }

    private static class MappedItem {

        private final String word;
        private final String file;

        public MappedItem(String word, String file) {
            this.word = word;
            this.file = file;
        }

        public String getWord() {
            return word;
        }

        public String getFile() {
            return file;
        }

        @Override
        public String toString() {
            return "[\"" + word + "\",\"" + file + "\"]";
        }
    }
}

