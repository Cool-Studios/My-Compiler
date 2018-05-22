package compiler.phases.liveness;

import compiler.phases.frames.Frames;
import compiler.phases.frames.Temp;
import compiler.phases.imcgen.ImcGen;

import java.util.LinkedList;

/**
 *      Interference graph variables.
 *
 * @author haytham
 */
public class InterferenceGraph {

    private LinkedList<Node> nodes;


    public InterferenceGraph() {
        this.nodes = new LinkedList<>();
    }


    // Temp vars for the interference graph
    public void addTemps(Temp t1, Temp t2) {
        if (t1.equals(ImcGen.FP) || t2.equals(ImcGen.FP) || t1.equals(ImcGen.SP) || t2.equals(ImcGen.SP)) {
            return;
        }

        Node node1 = addNode(t1);
        Node node2 = addNode(t2);
        node1.addNeighbor(node2);
        node2.addNeighbor(node1);
    }

    // Adding node to interference graph
    public Node addNode(Temp temp) {
        for (Node node : nodes) {
            if (node.temp.temp == temp.temp) {
                return node;
            }
        }

        Node newNode = new Node(temp);
        nodes.add(newNode);
        return newNode;
    }

    // Add the nodes
    public void addNode(Node node) {
        nodes.add(node);
    }


    // Add temps without interference to a graph
    public void addAllTemps() {
        for (Temp temp : Frames.allTemps) {
            addNode(temp);
        }
    }

    // Remove node from the interference graph
    public void remove(Node node) {
        for (Node neighbor : node.neighbors()) {
            neighbor.neighbors().remove(node);
        }
        nodes.remove(node);
    }

    // Provide the upper node with the lowest degree
    public Node lowDegNode(int k) {
        for (Node node : nodes) {
            if (node.deg() < k) {
                return node;
            }
        }
        return null;
    }

    public int numNodes() {
        return nodes.size();
    }

    // Display the matrix
    public void printAsMatrix() {
        System.out.print("    ");

        for (Node node : nodes) {
            System.out.printf("%5s", node.temp);
        }
        System.out.println();

        for (Node node : nodes) {
            System.out.printf("%5s", node.temp);
            for (Node node2 : nodes) {
                if (node.isNeighbor(node2)) {
                    System.out.print("    x");
                } else {
                    System.out.print("     ");
                }
            }
            System.out.println();
        }
    }

}


