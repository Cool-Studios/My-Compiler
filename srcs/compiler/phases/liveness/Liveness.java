package compiler.phases.liveness;

import compiler.phases.Phase;
import compiler.phases.asmgen.AsmGen;
import compiler.phases.lincode.CodeFragment;

import java.util.HashMap;

/**
 *      Liveness check by listing interference graphs.
 *
 * @author haytham
 */
public class Liveness extends Phase {

    public static final HashMap<CodeFragment, InterferenceGraph> graphs = new HashMap<>();

    public Liveness() {
        super("liveness");
    }

    // Generate graph for interference
    public void generate() {

        for (CodeFragment fragment : AsmGen.instrs.keySet()) {
            GraphGenerator generator = new GraphGenerator(AsmGen.instrs.get(fragment), fragment.frame);
            graphs.put(fragment, generator.createGraph());
        }

    }

    // Clear the interference graph
    public static void reset() {
        graphs.clear();
    }

    @Override
    public void close() {
        String loggedPhase = compiler.Main.cmdLineArgValue("--logged-phase");
        if ((loggedPhase != null) && loggedPhase.matches("liveness" + "|all")) {
            for (InterferenceGraph graph : graphs.values()) {
                graph.printAsMatrix();
                System.out.println();
            }
        }

    }
}
