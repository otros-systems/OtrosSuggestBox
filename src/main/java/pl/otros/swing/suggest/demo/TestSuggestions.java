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

package pl.otros.swing.suggest.demo;

import pl.otros.swing.suggest.SelectionListener;
import pl.otros.swing.suggest.SuggestDecorator;
import pl.otros.swing.suggest.SuggestionRenderer;
import pl.otros.swing.suggest.SuggestionSource;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

public class TestSuggestions {

  public static void main(String[] args) throws InvocationTargetException, InterruptedException {
    SwingUtilities.invokeAndWait(() -> {
      final JFrame frame = new JFrame("Test suggestions");

      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

      Container contentPane = frame.getContentPane();
      JPanel jPanel = new JPanel(new BorderLayout());
      JToolBar toolBar = new JToolBar("");
      JLabel jLabel = new JLabel("Enter file path: ");
      jLabel.setDisplayedMnemonic('p');
      final JTextField textField = new JTextField(40);

      jLabel.setLabelFor(textField);
      final JTextField component = new JTextField("aaaaaaassffsś");
      final JTextArea jTextArea = new JTextArea(4, 40);
      jTextArea.setEditable(true);
      jTextArea.setBorder(BorderFactory.createTitledBorder("Element description:"));

      SuggestionSource<File> suggestionSource = new StringSuggestionSource();
      SuggestionRenderer<File> suggestionRenderer = new FileSuggestionRenderer();

      SelectionListener<File> selectionListener = value -> {
        try {
          textField.getDocument().remove(0, textField.getText().length());
          textField.getDocument().insertString(0, value.getAbsolutePath(), null);
        } catch (BadLocationException e) {
          e.printStackTrace();
        }
        textField.setCaretPosition(textField.getText().length());
        jTextArea.setText("Type: " + (value.isDirectory() ? "Folder" : "File"));
      };

      SuggestDecorator.decorate(textField, suggestionSource, suggestionRenderer, selectionListener);


      final SuggestionSource<String> suggestionSource1 = query -> {
        final int caretLocation = query.getCaretLocation();
        final String value = query.getValue();
        if (value.length() > 0  && Character.isUpperCase(value.charAt(caretLocation-1))) {
          return Arrays.asList("1._", "2.!", "3.!D");
        } else {
          return Collections.emptyList();
        }
      };


      final SuggestionRenderer<String> suggestionRenderer1 = suggestion -> new JLabel(suggestion, iconForString(suggestion),SwingConstants.CENTER);
      final SelectionListener<String> selectionListener1 = value -> {
        final int caretPosition = jTextArea.getCaretPosition();
        String source = jTextArea.getText();
        String newValue = source.substring(0, caretPosition) + value + source.substring(caretPosition);
        jTextArea.setText(newValue);
        jTextArea.setCaretPosition(caretPosition + value.length());
      };
      SuggestDecorator.decorate(jTextArea, suggestionSource1, suggestionRenderer1, selectionListener1);

      textField.setText(File.listRoots()[0].getAbsolutePath());

      toolBar.add(jLabel);
      toolBar.add(textField);

      jPanel.add(toolBar, BorderLayout.NORTH);
      jPanel.add(new JScrollPane(jTextArea), BorderLayout.CENTER);
      jPanel.add(component, BorderLayout.SOUTH);
      contentPane.add(jPanel);

      frame.pack();
      frame.setVisible(true);
      textField.requestFocus();

    });


  }

  private static Icon iconForString(String s){
    float h = ((float)s.hashCode()%255)/255;
    final Color hsbColor = Color.getHSBColor(h, 1, 1);
    return new Icon(){

      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(hsbColor);
        if (s.startsWith("1")){
          g.fillOval(0,0,16,16);
        } else if (s.startsWith("2")){
          g.fillRoundRect(4,4,8,8,4,4);
        } else if (s.startsWith("2")){
          g.drawPolygon(new int[]{2,2,14},new int[]{2,14,14},3);
        } else {
          g.fillRect(3,3,10,5);
        }
      }

      @Override
      public int getIconWidth() {
        return 16;
      }

      @Override
      public int getIconHeight() {
        return 16;
      }
    };
  }
}
