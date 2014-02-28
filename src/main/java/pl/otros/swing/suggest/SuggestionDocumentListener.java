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
import javax.swing.FocusManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SuggestionDocumentListener<T> implements DocumentListener {


  private static final Object DOWN_ACTION = "Down action";
  private final JPanel suggestionPanel;
  private SuggestionRenderer<T> suggestionRenderer;
  private JTextField textField;
  private SuggestionSource<T> suggestionSource;
  private JWindow suggestionWindow;
  private SelectionListener<T> selectionListener;
  private JComponent[] suggestionComponents;

  @SuppressWarnings ("serial")
  public SuggestionDocumentListener(final JTextField textField, SuggestionSource<T> suggestionSource, SuggestionRenderer<T> suggestionRenderer, SelectionListener<T> selectionListener) {
    this.textField = textField;
    this.suggestionSource = suggestionSource;
    this.suggestionRenderer = suggestionRenderer;
    this.selectionListener = selectionListener;
    Window windowAncestor = SwingUtilities.getWindowAncestor(textField);
    suggestionWindow = new JWindow(windowAncestor);
    ComponentAdapter windowsSizeListener = new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        setSuggestionWindowLocation();
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        setSuggestionWindowLocation();
      }

    };
    suggestionPanel = new JPanel();
    JScrollPane suggestionScrollPane = new JScrollPane(suggestionPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    suggestionWindow.getContentPane().add(suggestionScrollPane);
    textField.addComponentListener(windowsSizeListener);
    windowAncestor.addComponentListener(windowsSizeListener);

    InputMap inputMap = textField.getInputMap();
    ActionMap actionMap = textField.getActionMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), DOWN_ACTION);
    actionMap.put(DOWN_ACTION, new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (suggestionComponents != null && suggestionComponents.length > 0) {
          suggestionComponents[0].requestFocus();
        }
      }
    });

    FocusAdapter hideSuggestionFocusAdapter = new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        Component oppositeComponent = e.getOppositeComponent();
        boolean focusOwner = textField.isFocusOwner();
        if (!focusOwner && !(oppositeComponent != null && oppositeComponent.getParent() == suggestionPanel)) {
          hideSuggestions();
        }
      }

    };
    textField.addFocusListener(hideSuggestionFocusAdapter);
    suggestionWindow.addFocusListener(hideSuggestionFocusAdapter);
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    makeSuggestions();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    makeSuggestions();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    makeSuggestions();
  }

  void makeSuggestions() {
    String text = textField.getText();
    List<T> suggestions = suggestionSource.getSuggestions(text);
    int suggestionsSize = suggestions.size();
    if (suggestionsSize == 0) {
      suggestionWindow.setVisible(false);
    } else {
      suggestionPanel.removeAll();
      suggestionPanel.revalidate();
      suggestionPanel.setLayout(new GridLayout(suggestionsSize, 1));
      suggestionComponents = new JComponent[suggestionsSize];
      int index = 0;
      for (final T suggestion : suggestions) {
        final JComponent suggestionComponent = suggestionRenderer.getSuggestionComponent(suggestion);
        suggestionComponents[index++] = suggestionComponent;
        suggestionComponent.setFocusable(true);
        suggestionComponent.setOpaque(true);
        suggestionComponent.setBorder(BorderFactory.createLineBorder(suggestionPanel.getBackground()));
        suggestionComponent.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 38) {
              //key up
              FocusManager.getCurrentManager().focusPreviousComponent();
            } else if (e.getKeyCode() == 40) {
              //key up
              FocusManager.getCurrentManager().focusNextComponent();
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              suggestionSelected(suggestion);
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
              hideSuggestions();
            }
          }

          @Override
          public void keyTyped(KeyEvent e) {
            textField.dispatchEvent(e);
            textField.requestFocus();
          }
        });
        suggestionComponent.addFocusListener(new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {
            highlightSuggestion(suggestionComponent);
          }

          @Override
          public void focusLost(FocusEvent e) {
            removeHighlightSuggestion(suggestionComponent);
          }
        });
        suggestionComponent.addMouseListener(new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            suggestionSelected(suggestion);
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            highlightSuggestion(suggestionComponent);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            removeHighlightSuggestion(suggestionComponent);
          }

        });


        suggestionPanel.add(suggestionComponent);

      }
      suggestionWindow.pack();
      setSuggestionWindowLocation();

      if (!suggestionWindow.isVisible()) {
        suggestionWindow.setFocusableWindowState(false);
        suggestionWindow.setVisible(true);
        suggestionWindow.setFocusableWindowState(true);
      }
    }

  }

  private void removeHighlightSuggestion(JComponent suggestionComponent) {
    suggestionComponent.setBorder(BorderFactory.createLineBorder(suggestionPanel.getBackground()));
  }


  private void highlightSuggestion(JComponent suggestion) {
    suggestion.setBorder(BorderFactory.createLineBorder(suggestion.getForeground()));
  }


  protected void hideSuggestions() {
    textField.requestFocus();
    suggestionWindow.setVisible(false);
  }

  protected void suggestionSelected(T suggestion) {
    selectionListener.selected(suggestion);
    textField.requestFocus();
  }

  private void setSuggestionWindowLocation() {
    suggestionWindow.setSize(textField.getWidth(), suggestionWindow.getHeight());
    int x = (int) textField.getLocationOnScreen().getX();
    int y = (int) (textField.getLocationOnScreen().getY() + textField.getHeight());
    suggestionWindow.setLocation(x, y);
  }
}
