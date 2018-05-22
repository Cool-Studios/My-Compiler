package compiler.phases.synan;

import common.report.Report;
import compiler.phases.Phase;
import compiler.phases.lexan.LexAn;
import compiler.phases.lexan.Symbol;
import compiler.phases.lexan.Term;
import compiler.phases.synan.dertree.DerLeaf;
import compiler.phases.synan.dertree.DerNode;
import compiler.phases.synan.dertree.DerTree;

/**
 * Syntax analysis.
 *
 * @author sliva
 */
public class SynAn extends Phase {

    /**
     * The constructed derivation tree.
     */
    private static DerTree derTree = null;

    /**
     * Returns the constructed derivation tree.
     *
     * @return The constructed derivation tree.
     */
    public static DerTree derTree() {
        return derTree;
    }

    /**
     * The lexical analyzer used by this syntax analyzer.
     */
    private final LexAn lexAn;

    /**
     * Constructs a new syntax analysis phase.
     */
    public SynAn() {
        super("synan");
        lexAn = new LexAn();
    }

    /**
     * The lookahead buffer (of length 1).
     */
    private Symbol currSymb = null;

    /**
     * Appends the current symbol in the lookahead buffer to the node of the
     * derivation tree that is currently being expanded by the parser.
     * <p>
     * Hence, the statement {@code currSymb = skip(node);} can be used for (a)
     * appending the current symbol in the lookahead buffer {@code currSymb} to
     * the node of the derivation tree and (b) eliminating this symbol from the
     * lookahead buffer.
     *
     * @param node The node of the derivation tree currently being expanded by
     *             the parser.
     * @return {@code null}.
     */
    private Symbol skip(DerNode node) {
        if (currSymb != null)
            node.add(new DerLeaf(currSymb));
        return null;
    }

    /**
     * The parser.
     * <p>
     * This method returns the derivation tree of the program in the source
     * file. It calls method {@link #parseSource()} that starts a recursive
     * descent parser implementation of an LL(1) parsing algorithm.
     *
     * @return The derivation tree.
     */
    public DerTree parser() {
        derTree = parseSource();
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        if (currSymb.token != Term.EOF)
            throw new Report.Error(currSymb, "Unexpected '" + currSymb + "' at the end of a program.");
        derTree.accept(new DerLogger(logger), null);

        return derTree;
    }

    @Override
    public void close() {
        lexAn.close();
        super.close();
    }

    /**
     * An improved skip method - also checks if the current symbol to append
     * matches the desired symbol.
     *
     * @param node  The node of the derivation tree currently being expanded by
     *              the parser.
     * @param token The token to check the current symbol's token against
     * @return {@code null}.
     */
    public Symbol check(DerNode node, Term token) {
        if (currSymb.token != token)
            throw new Report.Error(currSymb.location(), "Expected " + token + ", got " + currSymb.token);
        else
            node.add(new DerLeaf(currSymb));
        return null;

    }

    // --- PARSER ---

    /**
     * The root parsing method - creates a Source node and calls parseExpr
     * to parse the list of symbols.
     *
     * @return node
     */
    private DerNode parseSource() {
        DerNode node = new DerNode(Nont.Source);
        node.add(parseExpr());
        return node;
    }

    private DerNode parseExpr() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr);
        switch (currSymb.token) {

            // Literal
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NEW:
            case DEL:
            case IDENTIFIER:
            case LPARENTHESIS:
            case LBRACE:
            case LBRACKET:


            // Unary
            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
                node.add(parseExpr1());
                node.add(parseExpr0());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr");
        }
        return node;
    }

    private DerNode parseExpr0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr0);
        switch (currSymb.token) {

            // expr0 -> expr1 & expr0
            case XOR:
            case IOR:
                currSymb = skip(node);
                node.add(parseExpr1());
                node.add(parseExpr0());
                break;

            // expr0 -> Epsilon
            case RBRACKET:
            case RPARENTHESIS:
            case RBRACE:
            case COLON:
            case ASSIGN:
            case THEN:
            case DO:
            case END:
            case WHERE:
            case COMMA:
            case ELSE:
            case SEMIC:
            case EOF:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr0");

        }
        return node;
    }

    private DerNode parseExpr1() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr1);
        switch (currSymb.token) {

            // expr1 -> expr2 expr10
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NEW:
            case DEL:
            case IDENTIFIER:
            case LPARENTHESIS:
            case LBRACE:
            case LBRACKET:


            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
                node.add(parseExpr2());
                node.add(parseExpr10());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr1");

        }
        return node;
    }

    private DerNode parseExpr10() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr10);
        switch (currSymb.token) {

            // expr10 -> expr2 expr10
            case AND:
                currSymb = skip(node);
                node.add(parseExpr2());
                node.add(parseExpr10());
                break;

            // expr10 -> Epsilon
            case XOR:
            case IOR:
            case RBRACKET:
            case RPARENTHESIS:
            case RBRACE:
            case COLON:
            case ASSIGN:
            case THEN:
            case DO:
            case END:
            case WHERE:
            case COMMA:
            case ELSE:
            case SEMIC:
            case EOF:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr10");

        }
        return node;
    }

    private DerNode parseExpr2() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr2);
        switch (currSymb.token) {

            // expr2 -> expr3 expr20

            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NEW:
            case DEL:
            case IDENTIFIER:
            case LPARENTHESIS:
            case LBRACE:
            case LBRACKET:


            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
                node.add(parseExpr3());
                node.add(parseExpr20());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr2");

        }
        return node;
    }

    private DerNode parseExpr20() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr20);
        switch (currSymb.token) {

            // expr20 -> expr3 expr20
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case EQU:
            case NEQ:
                currSymb = skip(node);
                node.add(parseExpr3());
                node.add(parseExpr20());
                break;

            // expr20 -> Epsilon
            case XOR:
            case IOR:
            case AND:
            case RBRACKET:
            case RPARENTHESIS:
            case RBRACE:
            case COLON:
            case ASSIGN:
            case THEN:
            case DO:
            case END:
            case WHERE:
            case COMMA:
            case ELSE:
            case SEMIC:
            case EOF:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr20");

        }
        return node;
    }

    private DerNode parseExpr3() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr3);
        switch (currSymb.token) {

            // expr3 -> expr4 expr30
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NEW:
            case DEL:
            case IDENTIFIER:
            case LPARENTHESIS:
            case LBRACE:
            case LBRACKET:
            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
                node.add(parseExpr4());
                node.add(parseExpr30());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr3");

        }
        return node;
    }

    private DerNode parseExpr30() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr30);
        switch (currSymb.token) {

            // expr30 -> expr4 expr30 +-
            case ADD:
            case SUB:
                currSymb = skip(node);
                node.add(parseExpr4());
                node.add(parseExpr30());
                break;

            // expr30 -> Epsilon
            case XOR:
            case IOR:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case EQU:
            case NEQ:
            case AND:
            case RBRACKET:
            case RPARENTHESIS:
            case RBRACE:
            case COLON:
            case ASSIGN:
            case THEN:
            case DO:
            case END:
            case WHERE:
            case COMMA:
            case ELSE:
            case SEMIC:
            case EOF:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr30");

        }
        return node;
    }

    private DerNode parseExpr4() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr4);

        switch (currSymb.token) {
            // expr4 -> expr5 expr40
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NEW:
            case DEL:
            case IDENTIFIER:
            case LPARENTHESIS:
            case LBRACE:
            case LBRACKET:
            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
                node.add(parseExpr5());
                node.add(parseExpr40());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr4");

        }
        return node;
    }

    private DerNode parseExpr40() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr40);
        switch (currSymb.token) {

            // expr40 -> expr5 expr40 *\
            case MUL:
            case DIV:
            case MOD:
                currSymb = skip(node);
                node.add(parseExpr5());
                node.add(parseExpr40());
                break;

            // expr40 -> Epsilon
            case ADD:
            case SUB:
            case XOR:
            case IOR:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case EQU:
            case NEQ:
            case AND:
            case RBRACKET:
            case RPARENTHESIS:
            case RBRACE:
            case COLON:
            case ASSIGN:
            case THEN:
            case DO:
            case END:
            case WHERE:
            case COMMA:
            case ELSE:
            case SEMIC:
            case EOF:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr40");

        }
        return node;
    }

    private DerNode parseExpr5() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr5);
        switch (currSymb.token) {

            // expr5 -> expr6
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case IDENTIFIER:
            case LPARENTHESIS:
            case LBRACE:
                node.add(parseExpr6());
                break;

            // expr5 -> expr5
            case LBRACKET:
                currSymb = skip(node);
                node.add(parseType());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.RBRACKET);
                node.add(parseExpr5());
                break;

            // expr5 -> type new
            case NEW:
                currSymb = skip(node);
                node.add(parseType());
                break;

            // expr5 -> expr5 delete
            case DEL:

            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
                currSymb = skip(node);
                node.add(parseExpr5());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr5");

        }
        return node;
    }

    private DerNode parseExpr6() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr6);
        switch (currSymb.token) {

            // expr6 -> expr7 expr60
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case IDENTIFIER:
            case LPARENTHESIS:
            case LBRACE:
                node.add(parseExpr7());
                node.add(parseExpr60());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr6");

        }
        return node;
    }

    private DerNode parseExpr60() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr60);
        switch (currSymb.token) {

            // expr60 -> expr60 dot_expr
            case DOT:
                currSymb = skip(node);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.IDENTIFIER);
                node.add(parseExpr60());
                break;

            // expr60 -> expr60
            case LBRACKET:
                currSymb = skip(node);
                node.add(parseExpr());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.RBRACKET);
                node.add(parseExpr60());
                break;

            // expr60 -> Epsilon
            case MUL:
            case DIV:
            case MOD:
            case ADD:
            case SUB:
            case XOR:
            case IOR:
            case AND:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case EQU:
            case NEQ:
            case RBRACKET:
            case RPARENTHESIS:
            case RBRACE:
            case COLON:
            case ASSIGN:
            case THEN:
            case DO:
            case END:
            case WHERE:
            case COMMA:
            case ELSE:
            case SEMIC:
            case EOF:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr60");

        }
        return node;
    }

    private DerNode parseExpr7() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Expr7);
        switch (currSymb.token) {
            // expr7 -> expr*
            case IDENTIFIER:
                node.add(parseIdenExprMulti());
                break;

            // expr7 ->  expr parenthesis
            case LPARENTHESIS:
                currSymb = skip(node);
                node.add(parseExpr());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.RPARENTHESIS);
                break;

            // expr7 ->  *stateent & expr  Braces
            case LBRACE:
                currSymb = skip(node);
                node.add(parseStmtMulti());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.COLON);
                node.add(parseExprWhere());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.RBRACE);
                break;

            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
                currSymb = skip(node);
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Expr7");

        }
        return node;
    }


    private DerNode parseExprWhere() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.ExprWhere);
        switch (currSymb.token) {

            case LBRACE:
            case LPARENTHESIS:
            case LBRACKET:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
            case NEW:
            case DEL:
            case IDENTIFIER:
                node.add(parseExpr());
                node.add(parseExprWhere0());
                break;


            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_ExprWhere");

        }
        return node;
    }

    private DerNode parseExprWhere0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.ExprWhere0);
        switch (currSymb.token) {

            // exprwhere0 -> Epsilon
            case RBRACE:
                break;

            // dec*
            case WHERE:
                currSymb = skip(node);
                node.add(parseDeclMulti());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_ExprWhere0");

        }
        return node;
    }

    private DerNode parseExprMulti() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.ExprMulti);
        switch (currSymb.token) {

            // expr* -> expr*0
            case LBRACE:
            case LPARENTHESIS:
            case LBRACKET:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
            case NEW:
            case DEL:
            case IDENTIFIER:
                node.add(parseExpr());
                node.add(parseExprMulti0());
                break;

            case RPARENTHESIS:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_ExprMulti");

        }
        return node;
    }

    private DerNode parseExprMulti0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.ExprMulti0);
        switch (currSymb.token) {

            // expr*0 -> expr*0
            case COMMA:
                currSymb = skip(node);
                node.add(parseExpr());
                node.add(parseExprMulti0());
                break;

            // exprmulti0 → ε
            case RPARENTHESIS:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_ExprMulti0");

        }
        return node;
    }

    private DerNode parseExprAssign() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.ExprAssign);

        switch (currSymb.token) {

            // exprassignment -> Epsilon
            case RBRACE:
            case SEMIC:
                break;

            // exprassignment -> expr
            case ASSIGN:
                currSymb = skip(node);
                node.add(parseExpr());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_ExprAssign");

        }
        return node;
    }

    private DerNode parseType() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Type);
        switch (currSymb.token) {

            // type -> ident
            case IDENTIFIER:

                // type -> types <>
            case BOOL:
            case VOID:
            case CHAR:
            case INT:
                currSymb = skip(node);
                break;

            // type -> pointer
            case PTR:
                currSymb = skip(node);
                node.add(parseType());
                break;

            // type -> arr
            case ARR:
                currSymb = skip(node);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.LBRACKET);
                node.add(parseExpr());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.RBRACKET);
                node.add(parseType());
                break;

            // type -> rec
            case REC:
                currSymb = skip(node);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.LPARENTHESIS);

                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                if (currSymb.token == Term.RPARENTHESIS)
                    throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Type");

                node.add(parseIdenTypeMulti());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.RPARENTHESIS);
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Type");

        }
        return node;
    }

    private DerNode parseStmt() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Stmt);
        switch (currSymb.token) {

            // statement -> expr statement0
            case LBRACE:
            case LBRACKET:
            case LPARENTHESIS:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NEW:
            case DEL:
            case IDENTIFIER:
            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
                node.add(parseExpr());
                node.add(parseStmt0());
                break;

            case IF:
                currSymb = skip(node);
                node.add(parseExpr());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.THEN);
                node.add(parseStmtMulti());
                node.add(parseStmtElse());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.END);
                break;

            case WHILE:
                currSymb = skip(node);
                node.add(parseExpr());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.DO);
                node.add(parseStmtMulti());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.END);
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Stmt");

        }
        return node;
    }

    private DerNode parseStmt0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Stmt0);
        switch (currSymb.token) {

            // statement0 -> expr
            case ASSIGN:
                currSymb = skip(node);
                node.add(parseExpr());
                break;

            // statement0 → Epsilon
            case END:
            case SEMIC:
            case ELSE:
            case COLON:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Stmt0");

        }
        return node;
    }

    private DerNode parseStmtMulti() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.StmtMulti);
        switch (currSymb.token) {

            case LBRACE:
            case LBRACKET:
            case LPARENTHESIS:
            case BOOLCONST:
            case CHARCONST:
            case INTCONST:
            case PTRCONST:
            case VOIDCONST:
            case NEW:
            case DEL:
            case NOT:
            case ADD:
            case SUB:
            case MEM:
            case VAL:
            case IF:
            case WHILE:
            case IDENTIFIER:
                node.add(parseStmt());
                node.add(parseStmtMulti0());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parseStmtMulti");

        }
        return node;
    }

    private DerNode parseStmtMulti0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.StmtMulti0);
        switch (currSymb.token) {

            case COLON:
            case ELSE:
            case END:
                break;

            case SEMIC:
                currSymb = skip(node);
                node.add(parseStmtMulti());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_StmtMulti0");

        }
        return node;
    }

    private DerNode parseStmtElse() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.StmtElse);
        switch (currSymb.token) {

            case ELSE:
                currSymb = skip(node);
                node.add(parseStmtMulti());
                break;


            case END:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parseStmtElse");

        }
        return node;
    }

    private DerNode parseDecl() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.Decl);

        switch (currSymb.token) {
            case TYP:
                currSymb = skip(node);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.IDENTIFIER);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.COLON);
                node.add(parseType());
                break;

            case VAR:
                currSymb = skip(node);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.IDENTIFIER);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.COLON);
                node.add(parseType());
                break;

            case FUN:
                currSymb = skip(node);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.IDENTIFIER);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.LPARENTHESIS);
                node.add(parseIdenTypeMulti());
                currSymb = check(node, Term.RPARENTHESIS);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.COLON);
                node.add(parseType());
                node.add(parseExprAssign());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_Decl");

        }
        return node;
    }

    private DerNode parseDeclMulti() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.DeclMulti);

        switch (currSymb.token) {
            case TYP:
            case VAR:
            case FUN:
                node.add(parseDecl());
                node.add(parseDeclMulti0());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_DeclMulti");

        }
        return node;
    }

    private DerNode parseDeclMulti0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.DeclMulti0);

        switch (currSymb.token) {

            case SEMIC:
                currSymb = skip(node);
                node.add(parseDeclMulti());
                break;

            case RBRACE:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_DeclMulti0");

        }
        return node;
    }

    private DerNode parseIdenTypeMulti() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.IdenTypeMulti);

        switch (currSymb.token) {

            case IDENTIFIER:
                currSymb = skip(node);
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.COLON);
                node.add(parseType());
                node.add(parseIdenTypeMulti0());
                break;

            case RPARENTHESIS:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_IdenTypeMulti");

        }
        return node;
    }

    private DerNode parseIdenTypeMulti0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.IdenTypeMulti0);

        switch (currSymb.token) {
            case COMMA:
                currSymb = skip(node);
                node.add(parseIdenTypeMulti());
                break;

            case RPARENTHESIS:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_IdenTypeMulti0");

        }
        return node;
    }

    private DerNode parseIdenExprMulti() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.IdenExprMulti);
        switch (currSymb.token) {

            case IDENTIFIER:
                currSymb = skip(node);
                node.add(parseIdenExprMulti0());
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_IdenExprMulti");

        }
        return node;
    }

    private DerNode parseIdenExprMulti0() {
        currSymb = currSymb == null ? lexAn.lexer() : currSymb;
        DerNode node = new DerNode(Nont.IdenExprMulti0);

        switch (currSymb.token) {

            case LPARENTHESIS:
                currSymb = skip(node);
                node.add(parseExprMulti());
                currSymb = currSymb == null ? lexAn.lexer() : currSymb;
                currSymb = check(node, Term.RPARENTHESIS);
                break;

            case LBRACKET:
            case IDENTIFIER:
            case COLON:
            case RBRACE:
            case XOR:
            case IOR:
            case RBRACKET:
            case RPARENTHESIS:
            case ASSIGN:
            case END:
            case DO:
            case THEN:
            case WHERE:
            case COMMA:
            case ELSE:
            case SEMIC:
            case ADD:
            case SUB:
            case AND:
            case LTH:
            case GTH:
            case LEQ:
            case GEQ:
            case EQU:
            case NEQ:
            case MUL:
            case DIV:
            case MOD:
            case DOT:
            case EOF:
                break;

            default:
                throw new Report.Error(currSymb.location(), "Unrecognized symbol " + currSymb.stringify() + " in parse_IdenExprMulti0");

        }
        return node;
    }

}
