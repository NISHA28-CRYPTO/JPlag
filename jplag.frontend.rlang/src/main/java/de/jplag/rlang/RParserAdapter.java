package de.jplag.rlang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import de.jplag.AbstractParser;
import de.jplag.Token;
import de.jplag.TokenType;
import de.jplag.SharedTokenType;
import de.jplag.rlang.grammar.RFilter;
import de.jplag.rlang.grammar.RLexer;
import de.jplag.rlang.grammar.RParser;

import static de.jplag.Token.NO_VALUE;

/**
 * This class sets up the lexer and parser generated by ANTLR4, feeds the submissions through them and passes the
 * selected tokens on to the main program.
 */
public class RParserAdapter extends AbstractParser {

    private String currentFile;
    private List<Token> tokens;

    /**
     * Creates the RParserAdapter
     */
    public RParserAdapter() {
        super();
    }

    /**
     * Parsers a list of files into a single token list of {@link Token}s.
     * @param directory the directory of the files.
     * @param fileNames the file names of the files.
     * @return a list containing all tokens of all files.
     */
    public List<Token> parse(File directory, String[] fileNames) {
        tokens = new ArrayList<>();
        errors = 0;
        for (String fileName : fileNames) {
            if (!parseFile(directory, fileName)) {
                errors++;
            }
            tokens.add(new RToken(SharedTokenType.FILE_END, fileName, NO_VALUE, NO_VALUE, NO_VALUE));
        }
        return tokens;
    }

    private boolean parseFile(File directory, String fileName) {
        File file = new File(directory, fileName);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            currentFile = fileName;

            // create a lexer, a parser and a buffer between them.
            RLexer lexer = new RLexer(CharStreams.fromStream(inputStream));
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RFilter filter = new RFilter(tokens);
            filter.stream();
            tokens.seek(0);

            RParser parser = new RParser(tokens);

            // Create a tree walker and the entry context defined by the parser grammar
            ParserRuleContext entryContext = parser.prog();
            ParseTreeWalker treeWalker = new ParseTreeWalker();

            // Walk over the parse tree:
            for (int i = 0; i < entryContext.getChildCount(); i++) {
                ParseTree parseTree = entryContext.getChild(i);
                treeWalker.walk(new JplagRListener(this), parseTree);
            }
        } catch (IOException exception) {
            logger.error("Parsing Error in '" + fileName + "': " + File.separator + exception.getMessage(), exception);
            return false;
        }
        return true;
    }

    /**
     * Adds a new {@link Token} to the current token list.
     * @param type the type of the new {@link Token}
     * @param line the line of the Token in the current file
     * @param start the start column of the Token in the line
     * @param length the length of the Token
     */
    /* package-private */ void addToken(TokenType type, int line, int start, int length) {
        tokens.add(new RToken(type, currentFile, line, start, length));

    }
}
