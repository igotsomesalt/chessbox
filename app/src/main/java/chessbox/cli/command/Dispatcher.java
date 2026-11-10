package chessbox.cli.command;

import java.util.List;

import chessbox.cli.parse.MissingLeafCommandException;
import chessbox.cli.parse.UnknownLeafCommandException;
import chessbox.cli.token.Token;

public interface Dispatcher {

    default void dispatch(String parent, List<Token> args, CommandRegistry leafCommands) throws Exception {
        if (args.isEmpty()) throw new MissingLeafCommandException(parent);
        String leaf = args.get(0).lexeme();
        Command leafCommand = leafCommands.get(leaf);
        if (leafCommand == null) throw new UnknownLeafCommandException(leaf);
        leafCommand.execute(args.subList(1, args.size()));
    }
}
