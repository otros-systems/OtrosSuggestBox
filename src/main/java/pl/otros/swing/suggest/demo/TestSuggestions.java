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
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class TestSuggestions {


  public static void main(String[] args) throws InvocationTargetException, InterruptedException {
    SwingUtilities.invokeAndWait(new Runnable() {

      @Override
      public void run() {
        final JFrame frame = new JFrame("Test suggestions");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Container contentPane = frame.getContentPane();
        JPanel jPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar("");
        JLabel jLabel = new JLabel("Enter file path: ");
        jLabel.setDisplayedMnemonic('p');
        final JTextField textField = new JTextField(40);
        jLabel.setLabelFor(textField);
        final JTextArea jTextArea = new JTextArea(4, 40);
        jTextArea.setBorder(BorderFactory.createTitledBorder("Element description:"));
        SuggestionSource<File> suggestionSource = new StringSuggestionSource();

        SuggestionRenderer<File> suggestionRenderer = new FileSuggestionRenderer();


        SelectionListener<File> selectionListener = new SelectionListener<File>() {

          @Override
          public void selected(File value) {
            textField.setText(value.getAbsolutePath());
            jTextArea.setText("Type: " + (value.isDirectory() ? "Folder" : "File"));
          }
        };

        SuggestDecorator.decorate(textField, suggestionSource, suggestionRenderer, selectionListener);

        textField.setText(File.listRoots()[0].getAbsolutePath());

        toolBar.add(jLabel);
        toolBar.add(textField);


        jPanel.add(toolBar, BorderLayout.NORTH);
        jPanel.add(jTextArea, BorderLayout.CENTER);
        contentPane.add(jPanel);

        frame.pack();
        frame.setVisible(true);
        textField.requestFocus();


      }
    });

  }

}
