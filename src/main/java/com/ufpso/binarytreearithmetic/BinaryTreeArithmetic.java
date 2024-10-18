package com.ufpso.binarytreearithmetic;

import java.util.ArrayList;

public class BinaryTreeArithmetic {

    public static void main(String[] args) {
        String expr = "5 + 3*4 - 2";
        System.out.println("Expr: " + expr);

        ArrayList<Token> tokens;
        try {
            tokens = BinaryTreeArithmetic.tokenize(expr);
        } catch (InvalidCharException e) {
            System.out.println(e);
            return;
        }

        for (Token token : tokens) {
            if (token.type == Token.Type.NUMBER) {
                System.out.printf("Number: %f\n", token.valueNumber);
                continue;
            }

            if (token.type == Token.Type.OPERATOR) {
                System.out.printf("Operator: %s\n", token.valueOperator);
                continue;
            }
        }

        BinaryTree tree = toBinaryTree(tokens);

        System.out.printf("Result: %f\n", executeBinaryTree(tree));
    }

    public static double executeBinaryTree(BinaryTree tree) {
        if (tree.token.type == Token.Type.NUMBER) {
            // Este DEBE SER un nodo hoja.
            return tree.token.valueNumber;
        }

        // En este punto, somos un operador.
        double left = tree.left.token.valueNumber;
        if (tree.left.token.type != Token.Type.NUMBER) {
            left = executeBinaryTree(tree.left);
        }

        double right = tree.right.token.valueNumber;
        if (tree.right.token.type != Token.Type.NUMBER) {
            right = executeBinaryTree(tree.right);
        }

        return executeOperand(left, tree.token.valueOperator, right);
    }

    public static double executeOperand(double left, Token.Operator operator, double right) {
        if (operator == Token.Operator.ADD) {
            return left + right;
        }

        if (operator == Token.Operator.SUB) {
            return left - right;
        }

        if (operator == Token.Operator.MUL) {
            return left * right;
        }

        if (operator == Token.Operator.DIV) {
            return left / right;
        }

        return 99999999999999.77777777777; // Para saber fácilmente que hubo un error.
    }

    public static BinaryTree toBinaryTree(ArrayList<Token> tokens) {
        BinaryTree tree = null;

        for (int i = 0; i < tokens.size(); i++) {
            if (tree == null) {
                if (tokens.get(i).type != Token.Type.NUMBER) {
                    // El primer token debe ser un número.
                    System.out.println("TODO: Add exception.");
                    return tree;
                }

                tree = new BinaryTree(tokens.get(i));
                continue;
            }

            if (tokens.get(i).type == Token.Type.OPERATOR) {
               // El token anterior debió ser un número, por lo que se debe
               // añadir el operador como un nodo padre del actual.

               BinaryTree oldTree = tree;
               tree = new BinaryTree(tokens.get(i));
               tree.left = oldTree;

               continue;
            }

            // Ahora el nodo debe ser un número.

            BinaryTree node = null;

            if (i+1 < tokens.size()
                  && (tokens.get(i+1).valueOperator == Token.Operator.MUL
                  || tokens.get(i+1).valueOperator == Token.Operator.DIV)) {
               // Si el token siguiente al actual es un operador con
               // prioridad a la suma o resta, crear un nuevo nodo con
               // el caso especial.

               node = new BinaryTree(tokens.get(i+1));
               node.left = new BinaryTree(tokens.get(i));
               node.right = new BinaryTree(tokens.get(i+2));
               i+=2;
            } else {
               // El operador no tiene prioridad normal, se anexa sin
               // problemas.

               node = new BinaryTree(tokens.get(i));
            }

            // Buscar un lugar para el nodo.
            if (tree.left == null) {
               tree.left = node;
               continue;
            }

            if (tree.right == null) {
               tree.right = node;
               continue;
            }

            // No hay espacio abajo, añadir el nodo arriba.
            BinaryTree oldTree = tree;
            tree = node;
            tree.right = oldTree;
        }

        return tree;
    }

    public static ArrayList<Token> tokenize(String expr) throws InvalidCharException {
        ArrayList<Token> tokens = new ArrayList<Token>();
        for (int i = 0; i < expr.length(); i++) {
            if (expr.charAt(i) == ' ') {
                continue;
            }

            if (expr.charAt(i) == '+') {
                tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.ADD));
                continue;
            }
            if (expr.charAt(i) == '-') {
                tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.SUB));
                continue;
            }
            if (expr.charAt(i) == '*') {
                tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.MUL));
                continue;
            }
            if (expr.charAt(i) == '/') {
                tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.DIV));
                continue;
            }

            boolean parsingNumber = false;
            double number = 0;
            while (i < expr.length() && BinaryTreeArithmetic.isDigit(expr.charAt(i))) {
                parsingNumber = true;
                number += BinaryTreeArithmetic.char2digit(expr.charAt(i));
                number *= 10;
                i++;
            }
            if (parsingNumber) {
                number /= 10;
                i--;
                tokens.add(new Token(Token.Type.NUMBER, number));
                continue;
            }

            // Si llegamos hasta aquí, es porque hay caracteres inválidos.
            throw new InvalidCharException(i, expr.charAt(i));
        }

        return tokens;
    }

    private static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private static double char2digit(char c) {
        return c - '0';
    }
}