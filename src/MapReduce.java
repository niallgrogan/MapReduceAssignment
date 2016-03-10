/*
 * Niall Grogan - 12429338
 * Stephen Dooley - 12502947
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapReduce {

    public static void main(String[] args) {

        //TODO make this a command line input
        int poolSize = args.length;

        //PART 1 - Taking input from a list of text files
        Map<String, String> input = new HashMap<String, String>();
        for(String fileName:args) {
            String everything = "";
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                everything = sb.toString();
                br.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            input.put(fileName,everything);
        }


        // APPROACH #1: Brute force
        {
            Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();

            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            while(inputIter.hasNext()) {
                Map.Entry<String, String> entry = inputIter.next();
                String file = entry.getKey();
                String contents = entry.getValue();

                String[] words = contents.trim().split("\\s+");

                for(String word : words) {

                    Map<String, Integer> files = output.get(word);
                    if (files == null) {
                        files = new HashMap<String, Integer>();
                        output.put(word, files);
                    }

                    Integer occurrences = files.remove(file);
                    if (occurrences == null) {
                        files.put(file, 1);
                    } else {
                        files.put(file, occurrences.intValue() + 1);
                    }
                }
            }

            // show me:
            System.out.println(output);
        }


        // APPROACH #2: MapReduce
        {
            Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();

            // MAP:

            List<MappedItem> mappedItems = new LinkedList<MappedItem>();

            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            while(inputIter.hasNext()) {
                Map.Entry<String, String> entry = inputIter.next();
                String file = entry.getKey();
                String contents = entry.getValue();

                map(file, contents, mappedItems);
            }

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

            Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
            while(groupedIter.hasNext()) {
                Map.Entry<String, List<String>> entry = groupedIter.next();
                String word = entry.getKey();
                List<String> list = entry.getValue();

                reduce(word, list, output);
            }

            System.out.println(output);
        }


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
            long mapTimer = 0;
            //Not sure if this is good practice
            while (!mapPool.isTerminated()) {
                mapTimer++;
            }
            System.out.println("Finished all map threads in " + mapTimer + " iterations");

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
            long reduceTimer = 0;
            //Not sure if this is good practice
            while (!reducePool.isTerminated()) {
                reduceTimer++;
            }
            System.out.println("Finished all reduce threads in " + reduceTimer + " iterations");

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

