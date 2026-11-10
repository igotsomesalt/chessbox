package chessbox.cli.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokenizer {

    private Tokenizer() {}
    
    public static List<Token> tokenize(String command) {

        List<String> tokens = Arrays.asList(command.split(" "));
        List<Token> newTokens = new ArrayList<>();

        for (int i = 0; i <= tokens.size(); i++) {

            if (i == tokens.size()) {
                newTokens.add(new Token(Token.Type.ENDLINE, ""));
                break;
            }

            String token = tokens.get(i);
            if (isNumber(token)) newTokens.add(new Token(Token.Type.NUMBER, token));
            else if (isOption(token)) newTokens.add(new Token(Token.Type.OPTION, token));
            else newTokens.add(new Token(Token.Type.WORD, token));
        }

        return newTokens;
    }


    private static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isOption(String token) {
        return token.startsWith("-");
    }
}
