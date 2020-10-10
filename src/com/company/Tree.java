package com.company;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Tree {
    private final Node root;
    private Integer nextId;

    public Tree(String name, String value) {
        root = new Node();
        root.id = 1;
        root.name = name;
        root.value = value;
        root.children = new ArrayList<>();
        nextId = 2;
    }

    private static class Node {
        private Integer id;
        private String name;
        private String value;
        private List<Node> children;
    }

    private Node findNodeById(Integer id, Node currentNode) {
        if (currentNode.id.equals(id)) return currentNode;

        Node result = null;

        for (int i=0;result == null && i<currentNode.children.size();i++) {
            result = findNodeById(id, currentNode.children.get(i));
        }

        return result;
    }

    public Integer addChildren(Integer id, String name, String value) {
        Node findedNode = findNodeById(id, root);

        Node added = new Node();
        added.id = nextId++;
        added.name = name;
        added.value = value;
        added.children = new ArrayList<>();

        findedNode.children.add(added);

        return added.id;
    }

    public void insertTreeInDB(Statement statement) {
        insertNode(root, 0, statement);
    }

    private void insertNode(Node currentNode, Integer parentId, Statement statement) {
        try {
            statement.executeUpdate("INSERT INTO `tree_table`(`node_id`, `parent_id`, `node_name`, `node_value`) VALUES (" + currentNode.id + "," + parentId + ",\"" + currentNode.name + "\",\"" + currentNode.value + "\")");
            System.out.println("Inserted: (" + currentNode.id + ", " + parentId + ", " + currentNode.name + ", " + currentNode.value + ")");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        for (int i=0;i<currentNode.children.size();i++) {
            insertNode(currentNode.children.get(i), currentNode.id, statement);
        }
    }
}
