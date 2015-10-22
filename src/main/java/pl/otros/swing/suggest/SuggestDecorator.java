/*
 * Copyright 2014 otros.systems@gmail.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package pl.otros.swing.suggest;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SuggestDecorator {

  public static <T> void decorate(final JTextComponent textField, SuggestionSource<T> suggestionSource, SuggestionRenderer<T> suggestionRenderer, SelectionListener<T>
          selectionListener) {
    Document document = textField.getDocument();
    SuggestionDocumentListener<? extends T> listener = new SuggestionDocumentListener<>(textField, suggestionSource, suggestionRenderer, selectionListener);
    document.addDocumentListener(listener);
    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        //do not select all on OSX after suggestion is selected
        if (e.getOppositeComponent() == null) {
          clearTextFieldSelectionAsync(textField);
        }
      }
    });

  }

  static void clearTextFieldSelectionAsync(final JTextComponent textField) {
    SwingUtilities.invokeLater(() -> {
      textField.select(0, 0);
      textField.setCaretPosition(textField.getText().length());
    });
  }
}
