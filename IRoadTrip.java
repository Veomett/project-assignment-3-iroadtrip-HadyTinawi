import java.io.*;
import java.util.*;
import java.util.regex.*;

public class IRoadTrip {

    public class Node implements Comparable<Node> { //creating the nodes for the graph
        String country;
        int distance;

        Node(String country, int distance) {
            this.country = country;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node obj) { //compare the nodes
            return Integer.compare(this.distance, obj.distance); //0: if (d == o) ; -1: if (d < o); 1: if (d > o)
        }
    }

    private Map<String, String> nameMapping = new HashMap<>(); //similar to professor's fixedCountries
    private Map<String, Map<String, Integer>> countryRoutes = new HashMap<>(); //nested hashmap for routes
    private Map<String, String> countryIDMapping = new HashMap<>(); //hashmap for ID
    private Map<String, Integer> countriesTravelDistance = new HashMap<>(); //hashmap for weights (countries)
    private Map<String, Map<String, Integer>> countryTravelDistance = new HashMap<>(); //hashmap for travel dist (country)

    public IRoadTrip(String[] inputArgs) {
        if (inputArgs.length == 3) {
            initializeNameMapping(); //make sure the names make sense
            readAndInitialize("capdist.csv"); //read file
            readAndMapNames("state_name.tsv"); //read file
            readAndProcessBorders("borders.txt"); //read file
        } else {
            System.out.println("Invalid number of arguments."); //need all 3 files, let user know and exit
            System.exit(0);
        }
    }

    private void initializeNameMapping() { //fixedCountries method (not all edge cases are handled)
        nameMapping.put("United States", "United States of America");
        nameMapping.put("US", "United States of America");
        nameMapping.put("Czechia", "Czech Republic");
        nameMapping.put("Korea, South", "Korea, Republic of");
        nameMapping.put("Korea, North", "Korea, People's Republic of");
        nameMapping.put("Timor-Leste", "East Timor");
        nameMapping.put("Cabo Verde", "Cape Verde");
        nameMapping.put("Cote d'Ivoire", "Ivory Coast");
        nameMapping.put("Gambia, The", "Gambia");
        nameMapping.put("Bahamas, The", "Bahamas");
        nameMapping.put("Czechia", "Czech Republic");
        nameMapping.put("The Central African Republic", "Central African Republic");
        nameMapping.put("Congo, Republic of the", "Congo");
        nameMapping.put("The Republic of the Congo", "Congo");
        nameMapping.put("The Slovak Republic", "Slovakia");
        nameMapping.put("Denmark (Greenland)", "Denmark");
        nameMapping.put("Yemen", "Yemen (Arab Republic of Yemen");
        nameMapping.put("Tanzania", "Tanzania/Tanganyika");
        nameMapping.put("Vietnam", "Vietnam, Democratic Republic of");
        nameMapping.put("The Solomon Islands", "Solomon Islands");
        nameMapping.put("UK", "United Kingdom");
        nameMapping.put("Germany", "German Federal Republic");
        nameMapping.put("Spain (Ceuta)", "Spain");
        nameMapping.put("Morocco (Cueta)", "Morocco");
        nameMapping.put("Italy", "Italy/Sardinia"); 
    }

    //need slicer[1,3,4]
    private void readAndInitialize(String fileName) { //read and initialize file (capdist)
        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int ignore = 0; //first line is the title for each column (need to ignore it)
            while ((line = fileReader.readLine()) != null) {
                if (ignore == 0) {
                    ignore++;
                    continue;
                }
                String[] slicer = line.split(","); //create the splitter
                String country1 = slicer[1].trim(); //ida
                String country2 = slicer[3].trim(); //idb
                int distance = Integer.parseInt(slicer[4].trim()); //dist in km

                countryTravelDistance.computeIfAbsent(country1, k -> new HashMap<>()).put(country2, distance);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readAndMapNames(String fileName) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) { //read state_name.tsv
            String line;
            while ((line = fileReader.readLine()) != null) {
                Pattern pattern = Pattern.compile("\\b[\\S ]+\\b"); //create pattern
                Matcher matcher = pattern.matcher(line); //create matcher

                if (matcher.find()) { //find the keys
                    String keyValue = search(matcher, 2);
                    String key = search(matcher, 2);
                    countryIDMapping.put(key, keyValue); //put them in ID map
                }
            }
        } catch (IOException e) { //handle error
            e.printStackTrace();
        }
    }
    //we only need the countries, the delimiters are present (slice them)
    private void readAndProcessBorders(String fileName) { //read borders.txt
        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] slicer = line.split("=|;"); //split the line
                String country1 = slicer[0].trim(); //get the first value and check if the name needs updating
                if (nameMapping.containsKey(country1)) {
                    country1 = nameMapping.get(country1);
                }
                Map<String, Integer> borderCountries = new HashMap<>(); //create the Hashmap to store all the country's neighbors

                for (int i = 1; i < slicer.length; i++) {
                    String[] sliceBorders = slicer[i].trim().split("\\s+\\d[\\d,]*\\s+km"); //chatGPT help
                    String neighboringCountry = sliceBorders[0].trim();
                    if (nameMapping.containsKey(neighboringCountry)) {
                        neighboringCountry = nameMapping.get(neighboringCountry);
                    }
                    if (neighboringCountry != null) {
                        int length = getDistance(country1, neighboringCountry); //get the distance between the countries
                        if (length == -1) {
                            continue;
                        }
                        borderCountries.put(neighboringCountry, length); //put it in hashmap
                    }
                }
                countryRoutes.put(country1, borderCountries); //add the route
                System.out.println(country1 + " " + borderCountries); //print the country with its neighbor (just for observation and testing)
            }
        } catch (IOException e) { //handle exceptions
            e.printStackTrace();
        }
    }

    private String search(Matcher matcher, int position) { //search method used by matcher
        for (int i = 1; i < position; i++) {
            if (!matcher.find()) {
                return null;
            }
        }
        return matcher.group();
    }

    //Implementing Dikjstra's

    public int getDistance(String country1, String country2) { //get distance method
        String country1ID = countryIDMapping.get(country1); //get ida
        String country2ID = countryIDMapping.get(country2); //get idb

        if (country1ID != null && country2ID != null) { //if not null then get the distances
            Map<String, Integer> country1Distances = countryTravelDistance.get(country1ID);

            if (country1Distances != null) {
                Integer distance = country1Distances.get(country2ID);

                if (distance != null) {
                    return distance;
                }
            }
        }
        return -1;
    }

    public List<String> findPath(String firstCountry, String secondCountry) { //find the path
        Set<String> finalized = new HashSet<>(); //create a set for the countries visited 
        PriorityQueue<Node> Heap = new PriorityQueue<>(); //create the min Heap
        Map<String, Integer> dist = new HashMap<>(); //create the distances hashmap
        Map<String, String> previous = new HashMap<>(); //create the hashmap for the previous

        for (String node : countryRoutes.keySet()) { //looping over nodes
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(firstCountry, 0); //add to distance hashmap
        Heap.add(new Node(firstCountry, 0)); //add to min heap
        List<String> finalizedNodes = new ArrayList<>(); //create heap for finalized

        while (!Heap.isEmpty()) {
            Node currentNode = Heap.poll(); //peak only retrieves element, poll retrieves element and then removes
            String current = currentNode.country;

            if (!finalized.contains(current)) {
                finalized.add(current);
            }

            if (countryRoutes.containsKey(current)) {
                for (Map.Entry<String, Integer> neighbor : countryRoutes.get(current).entrySet()) {
                    String adjacentCountry = neighbor.getKey();
                    int weight = neighbor.getValue();
                    int newDistance = dist.get(current) + weight;

                    if (newDistance < dist.getOrDefault(adjacentCountry, Integer.MAX_VALUE)) {
                        dist.put(adjacentCountry, newDistance);
                        Heap.add(new Node(adjacentCountry, newDistance));
                        previous.put(adjacentCountry, current);
                    }
                }
            }
            if (current.equals(secondCountry)) {
                break;
            }
        }
        String currentCountry = secondCountry;
        while (!currentCountry.equals(firstCountry)) {
            String prevCountry = previous.get(currentCountry);
            if (dist.get(currentCountry) == null || dist.get(prevCountry) == null) {
                return null;
            }
            int distance = dist.get(currentCountry) - dist.get(prevCountry);
            finalizedNodes.add(prevCountry + " --> " + currentCountry + " (" + distance + "km.)\n");
            currentCountry = prevCountry;
        }
        Collections.reverse(finalizedNodes);
        return finalizedNodes;
    }
    //TODO: write acceptUser that satisfies the requirement 3 thingy and print output
    public void acceptUserInput() {
        try {
            try (Scanner scan = new Scanner(System.in)) {
                initializeNameMapping();
                while (true) {
                    System.out.println("Enter the name of the first country (type EXIT to quit): ");
                    String firstCountry = scan.nextLine().trim();
                    if (nameMapping.containsKey(firstCountry)) {
                        firstCountry = nameMapping.get(firstCountry);
                    }
                    if (firstCountry.equals("EXIT") || firstCountry.equals("exit")) {
                        break;
                    } else if (!countryRoutes.containsKey(firstCountry)) {
                        System.out.println("Invalid country name. Please enter a valid country name");
                        continue;
                    }
                    System.out.println("Enter the name of the second country (type EXIT to quit): ");
                    String secondCountry = scan.nextLine().trim();
                    if (nameMapping.containsKey(secondCountry)) {
                        secondCountry = nameMapping.get(secondCountry);
                    }
                    if (!countryRoutes.containsKey(secondCountry)) {
                        System.out.println("Invalid country name. Please enter a valid country name");
                        continue;
                    }
                    System.out.println("Route from " + firstCountry + " to " + secondCountry + ":\n " + findPath(firstCountry, secondCountry));
                    //scanner closes automatically when it goes out of scope
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] inputArgs) { //DONT CHANGE
        IRoadTrip journey = new IRoadTrip(inputArgs);
        journey.acceptUserInput();
    }

    public Map<String, Map<String, Integer>> getCountryRoutes() { 
        return countryRoutes;
    }
}

