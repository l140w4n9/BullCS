package ui;

import aggressor.Prefs;
import aggressor.ui.UseSynthetica;
import dialog.DialogManager;
import dialog.DialogUtils;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Navigator extends JComponent implements ListSelectionListener {
  protected CardLayout options = new CardLayout();
  
  protected JList navigator = new JList();
  
  protected JPanel switcher = new JPanel();
  
  protected boolean useAlternateValue = false;
  
  protected Map icons = new HashMap<>();
  
  public Navigator() {
    this.switcher.setLayout(this.options);
    this.navigator.setFixedCellWidth(125);
    setLayout(new BorderLayout());
    add(DialogUtils.wrapComponent(new JScrollPane(this.navigator), 5), "West");
    add(DialogUtils.wrapComponent(this.switcher, 5), "Center");
    this.navigator.setCellRenderer(new _A(this));
    this.navigator.addListSelectionListener(this);
    this.navigator.setModel(new DefaultListModel());
  }
  
  public void valueChanged(ListSelectionEvent paramListSelectionEvent) {
    this.options.show(this.switcher, (String) this.navigator.getSelectedValue());
  }
  
  public void useAlternateValue(boolean paramBoolean) {
    this.useAlternateValue = paramBoolean;
    this.navigator.validate();
    this.navigator.repaint();
  }
  
  public void set(String paramString) {
    this.navigator.setSelectedValue(paramString, true);
    this.options.show(this.switcher, paramString);
  }
  
  public void addPage(String paramString1, Icon paramIcon, String paramString2, JComponent paramJComponent) {
    JPanel jPanel = new JPanel();
    jPanel.setLayout(new BorderLayout());
    jPanel.add(DialogUtils.description(paramString2), "North");
    jPanel.add(DialogUtils.top(paramJComponent), "Center");
    this.icons.put(paramString1, paramIcon);
    DefaultListModel<String> defaultListModel = (DefaultListModel)this.navigator.getModel();
    defaultListModel.addElement(paramString1);
    this.switcher.add(jPanel, paramString1);
  }
  
  public static void main(String[] paramArrayOfString) {
    (new UseSynthetica()).setup();
    JFrame jFrame = DialogUtils.dialog("Hello World", 640, 480);
    Navigator navigator = new Navigator();
    DialogManager dialogManager = new DialogManager(jFrame);
    dialogManager.startGroup("console");
    dialogManager.text("user", "User:", 20);
    dialogManager.text("pass", "Password:", 20);
    dialogManager.text("host", "Host:", 20);
    dialogManager.text("port", "Port:", 10);
    dialogManager.endGroup();
    navigator.addPage("Console", new ImageIcon("./resources/cc/black/png/monitor_icon&16.png"), "This is your opportunity to edit console preferences", dialogManager.layout("console"));
    dialogManager.startGroup("console2");
    dialogManager.text("user", "User A:", 20);
    dialogManager.text("pass", "Password:", 20);
    dialogManager.text("host", "Host:", 20);
    dialogManager.text("port", "Port:", 10);
    dialogManager.text("port", "Port:", 10);
    dialogManager.text("port", "Port:", 10);
    dialogManager.endGroup();
    navigator.addPage("Console II", new ImageIcon("./resources/cc/black/png/monitor_icon&16.png"), "This is another opportunity to edit stuff. I think you know the drill by now.", dialogManager.layout("console2"));
    jFrame.add(navigator, "Center");
    jFrame.add(DialogUtils.center(dialogManager.action("Close")), "South");
    jFrame.setVisible(true);
  }
  
  private class _A extends JLabel implements ListCellRenderer {
    private final Navigator A;

    private _A(Navigator this$0) {
      this.A = this$0; // 让 _A 持有外部类的实例
    }

    public Component getListCellRendererComponent(JList param1JList, Object param1Object, int param1Int, boolean param1Boolean1, boolean param1Boolean2) {
      String[] arrayOfString = param1Object.toString().split("!!");
      String str;
      if (arrayOfString == null || arrayOfString.length == 0) {
        str = "New Profile"; // 防止数组越界
      } else if (arrayOfString.length > 1) {
        str = arrayOfString[1]; // 使用备用值（如果存在）
      } else {
        str = "New Profile"; // 使用默认的第一个值
      }
      boolean bool = false;
      if ('*' == str.charAt(0)) {
        bool = true;
        str = str.substring(1);
      } 
      setText(str);
      setIcon((Icon)this.A.icons.get(param1Object));
      if (param1Boolean1) {
        setBackground(param1JList.getSelectionBackground());
        setForeground(param1JList.getSelectionForeground());
      } else {
        setBackground(param1JList.getBackground());
        setForeground(param1JList.getForeground());
      } 
      if (bool)
        setForeground(Prefs.getPreferences().getColor("connection.active.color", "#0000ff")); 
      setEnabled(param1JList.isEnabled());
      setFont(param1JList.getFont());
      setOpaque(true);
      return this;
    }
  }
}


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar\\ui\Navigator.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */