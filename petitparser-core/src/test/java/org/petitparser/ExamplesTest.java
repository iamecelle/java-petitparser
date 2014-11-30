package org.petitparser;

import org.junit.Test;
import org.petitparser.parser.Parser;

import static org.petitparser.Assertions.assertFailure;
import static org.petitparser.Assertions.assertSuccess;
import static org.petitparser.parser.characters.CharacterParser.any;
import static org.petitparser.parser.characters.CharacterParser.digit;
import static org.petitparser.parser.characters.CharacterParser.letter;
import static org.petitparser.parser.characters.CharacterParser.of;
import static org.petitparser.parser.characters.CharacterParser.whitespace;
import static org.petitparser.parser.characters.CharacterParser.word;
import static org.petitparser.parser.primitive.StringParser.of;

/**
 * Tests some small but realistic parser examples.
 */
public class ExamplesTest {

  static final Parser IDENTIFIER = letter().seq(word().star()).flatten();

  static final Parser NUMBER = of('-').optional()
      .seq(digit().plus())
      .seq(of('.').seq(digit().plus()).optional())
      .flatten();

  static final Parser STRING = of('"')
      .seq(any().starLazy(of('"')))
      .seq(of('"'))
      .flatten();

  static final Parser RETURN = of("return")
      .seq(whitespace().plus().flatten())
      .seq(IDENTIFIER.or(NUMBER).or(STRING))
      .pick(-1);

  static final Parser JAVADOC = of("/**")
      .seq(any().starLazy(of("*/")))
      .seq(of("*/"))
      .flatten();

  @Test
  public void testIdentifierSuccess() {
    assertSuccess(IDENTIFIER, "a", "a");
    assertSuccess(IDENTIFIER, "a1", "a1");
    assertSuccess(IDENTIFIER, "a12", "a12");
    assertSuccess(IDENTIFIER, "ab", "ab");
    assertSuccess(IDENTIFIER, "a1b", "a1b");
  }

  @Test
  public void testIdentifierIncomplete() {
    assertSuccess(IDENTIFIER, "a_", "a", 1);
    assertSuccess(IDENTIFIER, "a1-", "a1", 2);
    assertSuccess(IDENTIFIER, "a12+", "a12", 3);
    assertSuccess(IDENTIFIER, "ab ", "ab", 2);
  }

  @Test
  public void testIdentifierFailure() {
    assertFailure(IDENTIFIER, "", "letter expected");
    assertFailure(IDENTIFIER, "1", "letter expected");
    assertFailure(IDENTIFIER, "1a", "letter expected");
  }

  @Test
  public void testNumberPositiveSuccess() {
    assertSuccess(NUMBER, "1", "1");
    assertSuccess(NUMBER, "12", "12");
    assertSuccess(NUMBER, "12.3", "12.3");
    assertSuccess(NUMBER, "12.34", "12.34");
  }

  @Test
  public void testNumberNegativeSuccess() {
    assertSuccess(NUMBER, "-1", "-1");
    assertSuccess(NUMBER, "-12", "-12");
    assertSuccess(NUMBER, "-12.3", "-12.3");
    assertSuccess(NUMBER, "-12.34", "-12.34");
  }

  @Test
  public void testNumberIncomplete() {
    assertSuccess(NUMBER, "1..", "1", 1);
    assertSuccess(NUMBER, "12-", "12", 2);
    assertSuccess(NUMBER, "12.3.", "12.3", 4);
    assertSuccess(NUMBER, "12.34.", "12.34", 5);
  }

  @Test
  public void testNumberFailure() {
    assertFailure(NUMBER, "", "digit expected");
    assertFailure(NUMBER, "-", 1, "digit expected");
    assertFailure(NUMBER, "-x", 1, "digit expected");
    assertFailure(NUMBER, ".", "digit expected");
    assertFailure(NUMBER, ".1", "digit expected");
  }

  @Test
  public void testStringSuccess() {
    assertSuccess(STRING, "\"\"", "\"\"");
    assertSuccess(STRING, "\"a\"", "\"a\"");
    assertSuccess(STRING, "\"ab\"", "\"ab\"");
    assertSuccess(STRING, "\"abc\"", "\"abc\"");
  }

  @Test
  public void testStringIncomplete() {
    assertSuccess(STRING, "\"\"x", "\"\"", 2);
    assertSuccess(STRING, "\"a\"x", "\"a\"", 3);
    assertSuccess(STRING, "\"ab\"x", "\"ab\"", 4);
    assertSuccess(STRING, "\"abc\"x", "\"abc\"", 5);
  }

  @Test
  public void testStringFailure() {
    assertFailure(STRING, "\"", 1, "'\"' expected");
    assertFailure(STRING, "\"a", 2, "'\"' expected");
    assertFailure(STRING, "\"ab", 3, "'\"' expected");
    assertFailure(STRING, "a\"", "'\"' expected");
    assertFailure(STRING, "ab\"", "'\"' expected");
  }

  @Test
  public void testReturnSuccess() {
    assertSuccess(RETURN, "return f", "f");
    assertSuccess(RETURN, "return  f", "f");
    assertSuccess(RETURN, "return foo", "foo");
    assertSuccess(RETURN, "return    foo", "foo");
    assertSuccess(RETURN, "return 1", "1");
    assertSuccess(RETURN, "return  1", "1");
    assertSuccess(RETURN, "return -2.3", "-2.3");
    assertSuccess(RETURN, "return    -2.3", "-2.3");
    assertSuccess(RETURN, "return \"a\"", "\"a\"");
    assertSuccess(RETURN, "return  \"a\"", "\"a\"");
  }

  @Test
  public void testReturnFailure() {
    assertFailure(RETURN, "retur f", 0, "return expected");
    assertFailure(RETURN, "return1", 6, "whitespace expected");
    assertFailure(RETURN, "return  $", 8, "'\"' expected");
  }

  @Test
  public void testJavaDoc() {
    assertSuccess(JAVADOC, "/** foo */", "/** foo */");
    assertSuccess(JAVADOC, "/** * * */", "/** * * */");
  }

}
