package pl.otros.swing.suggest;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class StringInsertSuggestionListener implements SelectionListener<String> {
  @Override
  public void selected(SuggestionResult<String> result) {
    final Document document = result.getTextComponent().getDocument();
    try {
      document.insertString(result.getSuggestionSource().getCaretLocation(),result.getValue(),null);
    } catch (BadLocationException e) {
      //TODO
      e.printStackTrace();
    }
  }
}
