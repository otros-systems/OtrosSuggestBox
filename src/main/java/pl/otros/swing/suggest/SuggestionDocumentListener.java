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

class SuggestionDocumentListener<T> implements DocumentListener {


  private static final Object DOWN_ACTION = "Down action";
  private final JPanel suggestionPanel;
  private SuggestionRenderer<T> suggestionRenderer;
  private JTextField textField;
  private SuggestionSource<T> suggestionSource;
  private JWindow suggestionWindow;
  private SelectionListener<T> selectionListener;
  private JComponent[] suggestionComponents;

  private boolean fullyInitialized = false;
  private final ComponentAdapter windowsSizeListener;
  private final FocusAdapter hideSuggestionFocusAdapter;
  private JScrollPane suggestionScrollPane;

  @SuppressWarnings("serial")
  public SuggestionDocumentListener(final JTextField textField, SuggestionSource<T> suggestionSource, SuggestionRenderer<T> suggestionRenderer, SelectionListener<T> selectionListener) {
    this.textField = textField;
    this.suggestionSource = suggestionSource;
    this.suggestionRenderer = suggestionRenderer;
    this.selectionListener = selectionListener;

    windowsSizeListener = new ComponentAdapter() {

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
    InputMap inputMap = textField.getInputMap();
    ActionMap actionMap = textField.getActionMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), DOWN_ACTION);
    actionMap.put(DOWN_ACTION, new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (suggestionWindow != null && !suggestionWindow.isVisible()) {
          makeSuggestions();
        }
        if (suggestionComponents != null && suggestionComponents.length > 0) {
          suggestionComponents[0].requestFocus();
        }
      }
    });

    hideSuggestionFocusAdapter = new FocusAdapter() {

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
  }


  private void lazyInit() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(textField);
    suggestionWindow = new JWindow(windowAncestor);
    windowAncestor.addComponentListener(windowsSizeListener);
    textField.addComponentListener(windowsSizeListener);
    suggestionScrollPane = new JScrollPane(suggestionPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    suggestionWindow.getContentPane().add(suggestionScrollPane);
    suggestionWindow.addFocusListener(hideSuggestionFocusAdapter);
    fullyInitialized = true;
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
    if (SwingUtilities.getWindowAncestor(textField) == null){
      return;
    }
    if (!fullyInitialized) {
      lazyInit();
    }

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
            if (e.getKeyCode() == KeyEvent.VK_UP) {
              FocusManager.getCurrentManager().focusPreviousComponent();
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
              FocusManager.getCurrentManager().focusNextComponent();
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              suggestionSelected(suggestion);
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
              hideSuggestions();
              textField.requestFocus();
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
    for (JComponent toClearHighlight : suggestionComponents) {
      removeHighlightSuggestion(toClearHighlight);
    }
    suggestion.setBorder(BorderFactory.createLineBorder(suggestion.getForeground()));
    suggestionScrollPane.scrollRectToVisible(suggestion.getBounds());
  }


  protected void hideSuggestions() {
    if (suggestionWindow != null) {
      suggestionWindow.setVisible(false);
    }
  }

  protected void suggestionSelected(T suggestion) {
    selectionListener.selected(suggestion);
    textField.requestFocus();
  }

  private void setSuggestionWindowLocation() {
    suggestionWindow.setSize(textField.getWidth(), (int) Math.min(suggestionWindow.getHeight(), Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2));
    int x = (int) textField.getLocationOnScreen().getX();
    int y = (int) (textField.getLocationOnScreen().getY() + textField.getHeight());
    suggestionWindow.setLocation(x, y);
  }
}
