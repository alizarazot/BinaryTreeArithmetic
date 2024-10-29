package com.ufpso.binarytreearithmetic;

import java.util.ArrayList;

/*
 * Todos los nodos hoja son números, el resto de nodos son operadores aritméticos.
 */
public class BinaryTreeArithmetic {
  // executeBinaryTree toma un árbol binario, y ejecuta todas sus operaciones aritméticas (el
  // recorrido es in-orden).
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

  // executeOperand ejecuta la operación aritmética especificada.
  private static double executeOperand(double left, Token.Operator operator, double right) {
    switch (operator) {
      case Token.Operator.ADD:
        return left + right;
      case Token.Operator.SUB:
        return left - right;
      case Token.Operator.MUL:
        return left * right;
      case Token.Operator.DIV:
        return left / right;
      default:
        System.out.println("Program is broken...");
        System.out.printf("Invalid operator: '%s'.\n", operator);
        return (1 << 63) - 1;
    }
  }

  // toBinaryTree crea un árbol binario aritmético a partir de una lista de tokens.
  public static BinaryTree toBinaryTree(ArrayList<Token> tokens) {
    BinaryTree rootTree = null;

    // Suposiciones:
    //      1. El primer token es un número o paréntesis (otro operador lo romperá).
    //      2. Todos los paréntesis de apertura tienen su respectivo paréntesis de cierre.
    //      3. El último token es un número o paréntesis (otro operador lo romperá).
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).type == Token.Type.OPERATOR
          && tokens.get(i).valueOperator != Token.Operator.LPAR) {
        // Se asume que el token anterior debió ser un número, por lo que se debe
        // añadir el operador como un nodo padre del actual.

        BinaryTree oldTree = rootTree;
        rootTree = new BinaryTree(tokens.get(i));
        rootTree.left = oldTree;

        continue;
      }

      // Si el token siguiente al actual es un operador con
      // prioridad a la suma o resta, crear un nuevo nodo con
      // el caso especial.
      // Debido a la prioridad de operaciones, las multiplicaciones y divisiones siempre se añaden
      // como un hijo a la derecha del último nodo disponible.
      BinaryTree node = null;
      while (i + 1 < tokens.size()
          && (tokens.get(i + 1).valueOperator == Token.Operator.MUL
              || tokens.get(i + 1).valueOperator == Token.Operator.DIV)) {
        // Son varias multiplicaciones/divisiones seguidas.

        if (node == null) {
          // Asumimos que el nodo es un número.
          node = new BinaryTree(tokens.get(i));

          // Si no es un número, debe ser un paréntesis.
          if (tokens.get(i).valueOperator == Token.Operator.LPAR) {
            ArrayList<Token> subtokens = consumePar(tokens, i);
            // Sumamos 1 para saltar RPAR.
            i += subtokens.size() + 1;
            node = toBinaryTree(subtokens);
          }
        }

        // Estamos en el número, avanzar al operador.
        i++;
        Token operator = tokens.get(i);

        // Estamos en el operador, avanzar al siguiente número o paréntesis.
        i++;
        BinaryTree rightNode = new BinaryTree(tokens.get(i));

        // Si no es un número, debe ser un paréntesis.
        if (tokens.get(i).valueOperator == Token.Operator.LPAR) {
          ArrayList<Token> subtokens = consumePar(tokens, i);
          i += subtokens.size() + 1; // Sumamos 1 para quedar en RPAR.
          rightNode = toBinaryTree(subtokens);
        }

        // Añadir el operador como un padre del nodo actual.
        BinaryTree oldNode = node;
        node = new BinaryTree(operator);
        node.left = oldNode;
        node.right = rightNode;
      }
      if (node != null) {
        rootTree = addToTree(rootTree, node);
        continue;
      }

      if (tokens.get(i).type == Token.Type.NUMBER) {
        // Es un número escalar (sin más operadores).
        rootTree = addToTree(rootTree, new BinaryTree(tokens.get(i)));
        continue;
      }

      if (tokens.get(i).valueOperator == Token.Operator.LPAR) {
        ArrayList<Token> subtokens = consumePar(tokens, i);
        i += subtokens.size() + 1;
        node = toBinaryTree(subtokens);
        rootTree = addToTree(rootTree, node);
        continue;
      }
    }

    return rootTree;
  }

  // addToTree añade un nodo al árbol raíz, primero intenta añadirlo a la izquierda o derecha del
  // árbol raíz, si falla convierte el nodo en el nuevo árbol raíz y añade el antiguo árbol raíz
  // como uno de sus hijos.
  private static BinaryTree addToTree(BinaryTree rootTree, BinaryTree node) {
    if (rootTree == null) {
      return node;
    }

    // Buscar un lugar para el nodo.
    if (rootTree.left == null) {
      rootTree.left = node;
      return rootTree;
    }

    if (rootTree.right == null) {
      rootTree.right = node;
      return rootTree;
    }

    // No hay espacio abajo, añadir el nodo arriba.
    BinaryTree oldTree = rootTree;
    rootTree = node;
    rootTree.right = oldTree;

    return rootTree;
  }

  // consumePar toma una lista de tokens y el índice del paréntesis de apertura desde el cual se va
  // a iniciar y retorna otra lista conteniendo los tokens dentro de los paréntesis.
  private static ArrayList<Token> consumePar(ArrayList<Token> tokens, int i) {
    ArrayList<Token> subtokens = new ArrayList<Token>();

    int lparCount = 1; // Usado para llevar la cuenta de "sub-paréntesis".
    i++;
    while (lparCount != 0) {
      if (tokens.get(i).valueOperator == Token.Operator.LPAR) {
        lparCount++;
      }
      if (tokens.get(i).valueOperator == Token.Operator.RPAR) {
        lparCount--;
      }

      subtokens.add(tokens.get(i));
      i++;
    }

    subtokens.remove(subtokens.size() - 1);
    return subtokens;
  }

  // tokenize toma una expresión y la convierte a una lista de objetos con tokens (instrucciones).
  public static ArrayList<Token> tokenize(String expr) throws InvalidCharException {
    ArrayList<Token> tokens = new ArrayList<Token>();

    for (int i = 0; i < expr.length(); i++) {

      // Intentar hacer coincidir un operator y continuar con el siguiente caracter.
      switch (expr.charAt(i)) {
        case ' ':
          continue;
        case '+':
          tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.ADD));
          continue;
        case '-':
          tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.SUB));
          continue;
        case '*':
          tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.MUL));
          continue;
        case '/':
          tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.DIV));
          continue;
        case '(':
          tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.LPAR));
          continue;
        case ')':
          tokens.add(new Token(Token.Type.OPERATOR, Token.Operator.RPAR));
          continue;
      }

      // El caracter actual no es un operador, por lo que debe ser un número.
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

      // Si llegamos hasta aquí, es porque hay caracteres inválidos (como letras).
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
