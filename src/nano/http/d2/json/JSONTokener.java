package nano.http.d2.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JSONTokener {
    private final Reader reader;
    private long character;
    private boolean eof;
    private long index;
    private long line;
    private char previous;
    private boolean usePrevious;
    private long characterPreviousLine;

    public JSONTokener(Reader reader) {
        this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.characterPreviousLine = 0;
        this.line = 1;
    }

    public JSONTokener(String s) {
        this(new StringReader(s));
    }

    public void back() throws JSONException {
        if (this.usePrevious || this.index <= 0) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        this.decrementIndexes();
        this.usePrevious = true;
        this.eof = false;
    }

    private void decrementIndexes() {
        this.index--;
        if (this.previous == '\r' || this.previous == '\n') {
            this.line--;
            this.character = this.characterPreviousLine;
        } else if (this.character > 0) {
            this.character--;
        }
    }

    public boolean end() {
        return this.eof && !this.usePrevious;
    }

    public char next() throws JSONException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (IOException exception) {
                throw new JSONException(exception);
            }
        }
        if (c <= 0) {
            this.eof = true;
            return 0;
        }
        this.incrementIndexes(c);
        this.previous = (char) c;
        return this.previous;
    }

    protected char getPrevious() {
        return this.previous;
    }

    private void incrementIndexes(int c) {
        if (c > 0) {
            this.index++;
            if (c == '\r') {
                this.line++;
                this.characterPreviousLine = this.character;
                this.character = 0;
            } else if (c == '\n') {
                if (this.previous != '\r') {
                    this.line++;
                    this.characterPreviousLine = this.character;
                }
                this.character = 0;
            } else {
                this.character++;
            }
        }
    }

    public String next(int n) throws JSONException {
        if (n == 0) {
            return "";
        }
        char[] chars = new char[n];
        int pos = 0;
        while (pos < n) {
            chars[pos] = this.next();
            if (this.end()) {
                throw this.syntaxError("Substring bounds error");
            }
            pos += 1;
        }
        return new String(chars);
    }

    public char nextClean() throws JSONException {
        for (; ; ) {
            char c = this.next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    public String nextString(char quote) throws JSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            c = this.next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw this.syntaxError("Unterminated string");
                case '\\':
                    c = this.next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            try {
                                sb.append((char) Integer.parseInt(this.next(4), 16));
                            } catch (NumberFormatException e) {
                                throw this.syntaxError("Illegal escape.", e);
                            }
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw this.syntaxError("Illegal escape.");
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }

    public Object nextValue() throws JSONException {
        char c = this.nextClean();
        String string;
        switch (c) {
            case '"':
            case '\'':
                return this.nextString(c);
            case '{':
                this.back();
                try {
                    return new NanoJSON(this);
                } catch (StackOverflowError e) {
                    throw new JSONException("JSON Array or Object depth too large to process.", e);
                }
            case '[':
                this.back();
                try {
                    return new JSONArray(this);
                } catch (StackOverflowError e) {
                    throw new JSONException("JSON Array or Object depth too large to process.", e);
                }
        }
        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = this.next();
        }
        if (!this.eof) {
            this.back();
        }
        string = sb.toString().trim();
        if ("".equals(string)) {
            throw this.syntaxError("Missing value");
        }
        return NanoJSON.stringToValue(string);
    }

    public JSONException syntaxError(String message) {
        return new JSONException(message + this);
    }

    public JSONException syntaxError(String message, Throwable causedBy) {
        return new JSONException(message + this, causedBy);
    }

    @Override
    public String toString() {
        return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
    }
}
