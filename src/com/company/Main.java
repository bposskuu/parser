package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String inputText = readFromFile("./input.txt");

        inputText =  inputText.replace("\r\n", " ");

        String rootNodeName = "";

        Pattern p = Pattern.compile("(^\\w+) = \\{(.+)}");
        Matcher m = p.matcher(inputText);
        if (m.matches()) {
            rootNodeName = m.group(1);
            inputText = m.group(2);
        }
        else errorStructure();

        Tree tree = new Tree(rootNodeName, null);

        if (findNodes(inputText, tree, 1) != 1) errorStructure();

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n");

        try{
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            try (Connection conn = getConnection()){

                System.out.println("Connection to tree_parser DB successful!");

                Statement statement = conn.createStatement();

                tree.insertTreeInDB(statement);
            }
        }
        catch(Exception ex){
            System.out.println("Connection failed...");

            ex.printStackTrace();
        }

    }

    private static Integer findNodes(String text, Tree tree, Integer nodeId) {
        int i=0, k=0;
        boolean hasOneQuote = false;
        for (char symbol : text.toCharArray())
        {
            if (k>=i && k!=0) {
                i++;
                continue;
            }
            if(symbol == '{') {
                Pattern p = Pattern.compile("(\\w+) = \\{$");
                Matcher m = p.matcher(text.substring(0, i+1));
                if(!m.find()) errorStructure();
                String newNodeName = m.group(1);
                Integer newId = tree.addChildren(nodeId, newNodeName, null);
                k = findNodes(text.substring(i + 1), tree, newId) + i;
                if (k == i+1) errorStructure();
            }
            if(symbol == '}') {
                System.out.println("Funded node: " + text.substring(0, i) + "\n");
                return i+1;
            }
            if(symbol == '"') {
                if(!hasOneQuote) hasOneQuote = true;
                else {
                    Pattern p = Pattern.compile("(\\w+) = \"(\\w+)\"$");
                    Matcher m = p.matcher(text.substring(0, i+1));
                    if(!m.find()) errorStructure();
                    String newNodeName = m.group(1);
                    String newNodeValue = m.group(2);
                    tree.addChildren(nodeId, newNodeName, newNodeValue);
                    hasOneQuote = false;
                }
            }
            i++;
        }
        return 1;
    }

    private static String readFromFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private static Connection getConnection() throws SQLException, IOException{

        Properties props = new Properties();
        try(InputStream in = Files.newInputStream(Paths.get("database.properties"))){
            props.load(in);
        }
        String url = props.getProperty("url");
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        return DriverManager.getConnection(url, username, password);
    }

    private static void errorStructure() {
        System.out.println("Неверный формат данных");
        System.exit(1);
    }
}

