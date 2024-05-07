import java.util.*;
import java.io.IOException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WordLadderSolver {
    static class Node implements Comparable<Node> {
        String word;
        int cost;

        Node(String word, int cost) {
            this.word = word;
            this.cost = cost;
        }

        @Override
        public int compareTo(Node node) {
            if (cost < node.cost) { return -1; }
            else if (cost > node.cost) { return 1; }
            return 0;
        }
    }

    public static Map<String, List<String>> neighborMaps = new HashMap<>();
    public static Map<String, Boolean> isChecked = new HashMap<>();
    public static int visitedNode = 0;

    public static List<String> Solver(String start, String end, String algo) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        Map<String, Integer> costMap = new HashMap<>();
        Map<String, String> parentMap = new HashMap<>();

        int newCost = 0;
        if (!algo.equals("UCS")) { newCost += totalDiff(start, end); }
        priorityQueue.offer(new Node(start, newCost));
        costMap.put(start, newCost);
        parentMap.put(start, null);
        isChecked.put(start, true);

        while (!priorityQueue.isEmpty()) {
            visitedNode++;
            Node current = priorityQueue.poll();

            if (current.word.equals(end)) {
                // Reconstruct path
                List<String> path = new ArrayList<>();
                String word = end;
                while (word != null) {
                    path.add(word);
                    word = parentMap.get(word);
                }
                Collections.reverse(path);
                return path;
            }

            List<String> wordList = neighborMaps.get(current.word);

            for (String neighbor : wordList) {
                if (!isChecked.get(neighbor)) {
                    isChecked.put(neighbor, true);
                    if (algo.equals("UCS")) { newCost = costMap.get(current.word) + 1; }
                    else if (algo.equals("GBFS")) { newCost = totalDiff(neighbor, end); }
                    else /*AStar*/ { newCost = totalDiff(neighbor, end) + costMap.get(current.word) + 1; }
                    costMap.put(neighbor, newCost);
                    priorityQueue.offer(new Node(neighbor, newCost));
                    parentMap.put(neighbor, current.word);
                } 
            }
        }

        return null; // No path found
    }
    
    public static void neighborInitializer(String pathDict) {
        List<String> words;
        boolean isFormatted = false;
        String path = System.getProperty("user.dir") + "/../test/";
        if (Files.exists(Paths.get(path + "formatted/" + pathDict))) {
            pathDict = "formatted/" + pathDict;
            isFormatted = true;
        }
        
        try {
            words = Files.readAllLines(Paths.get(System.getProperty("user.dir") + "/../test/" + pathDict));
        } catch (IOException e) {
            return;
        }
        
        String currWord = "";
        if (isFormatted) {
            for (String word : words) {
                isChecked.put(word, false);
                if (currWord.equals("")) {
                    currWord = word;
                    neighborMaps.put(currWord, new ArrayList<>());
                } else if (word.equals("END")) {
                    currWord = "";
                } else {
                    neighborMaps.get(currWord).add(word);
                }
            }
        } else {
            for (String word : words) {
                isChecked.put(word, false);
                neighborMaps.put(word, new ArrayList<>());
            }
            
            int n = words.size();
            
            for (int i = 0; i < n-1; i++) {
                String word1 = words.get(i);
                System.out.println(word1);
                for (int j = i+1; j < n; j++) {
                    String word2 = words.get(j);
                    
                    if (totalDiff(word1, word2) == 1) {
                        neighborMaps.get(word1).add(word2);
                        neighborMaps.get(word2).add(word1);
                    }
                }
            }
            SaveDict(pathDict);
        }
    }
    
    private static int totalDiff(String word1, String word2) {
        if (word1.length() != word2.length())
            return -1;
        int diffCount = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i))
                diffCount++;
        }
        return diffCount;
    }

    private static void SaveDict(String pathDict) {
        FileWriter writer;
        try {
            writer = new FileWriter(System.getProperty("user.dir") + "/../test/formatted/" + pathDict);
            for (Map.Entry<String, List<String>> entry : neighborMaps.entrySet()) {
                writer.write(entry.getKey()+"\n");

                List<String> values = entry.getValue();
                for (String value: values) {
                    writer.write(value + "\n");
                }

                writer.write(";\n");
            }
            writer.close();
        } catch (IOException e) {
            return;
        }
    }

    public static void Reset() {
        for (Map.Entry<String, Boolean> entry : isChecked.entrySet()) {
            isChecked.put(entry.getKey(), false);
        }
        visitedNode = 0;
    }

    private static boolean isWord(String word) {
        return neighborMaps.containsKey(word);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String pathFile;

        System.out.print("Insert DictPath: ");
        pathFile = scanner.nextLine();
        if (pathFile.equals("")) { pathFile = "defaultDict.txt"; }
        System.out.println("Initializing dictionary...");
        neighborInitializer(pathFile);

        while (neighborMaps.isEmpty()) {
            System.out.println("Invalid Directory Path! Make sure the file is in 'test' folder.");
            System.out.print("Insert DictPath: ");
            pathFile = scanner.nextLine();
            if (pathFile.equals("")) { pathFile = "defaultDict.txt"; }
            neighborInitializer(pathFile);
        }
        
        boolean isLoop = true;
        while (isLoop) {
            System.out.print("Insert Root Word: ");
            String start = scanner.nextLine();
            while (!isWord(start)) {
                System.out.println("Invalid English Word!");
                System.out.print("Insert Root Word: ");
                start = scanner.nextLine();
            }
            System.out.print("Insert Target Word: ");
            String end = scanner.nextLine();
            while (!isWord(end)) {
                System.out.println("Invalid English Word!");
                System.out.print("Insert Target Word: ");
                start = scanner.nextLine();
            }
    
            boolean isValid = false;
            String mode = "";
            while (!isValid) {
                System.out.print("Insert Algorithm to Use (UCS / GBFS / AStar): ");
                mode = scanner.nextLine();
                if (mode.equals("UCS") || mode.equals("GBFS") || mode.equals("AStar")) { isValid = true; }
            }

            long startTime = System.currentTimeMillis();
            List<String> result = Solver(start, end, mode);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println("Algoritma " + mode);
            System.out.print("Path: ");
            System.out.println(result);
            System.out.println("Waktu eksekusi: " + duration + " ms");
            System.out.println("Banyak node yang dikunjungi: " + visitedNode);

            System.out.print("Wanna solve another word? (y/n): ");
            String input = scanner.nextLine();
            if (input.equals("n")) { isLoop = false; } else { Reset(); }
        }

        scanner.close();
    }
}
