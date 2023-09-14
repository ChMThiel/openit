package io.iotos;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

@ActionID(
        category = "File",
        id = "io.iotos.OpenFile"
)
@ActionRegistration(
        lazy = true,
        displayName = "#CTL_Openit"
)
@ActionReferences({
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1650),
    @ActionReference(path = "Shortcuts", name = "O-O")
})
@Messages("CTL_Openit=Open it")
public final class Openit extends AbstractAction {

    private static final long serialVersionUID = 1L;
    static Dialog dialog;

    @Override
    public void actionPerformed(ActionEvent e) {
        // does nothing, this is a popup menu
        DialogDisplayer d = Lookup.getDefault().lookup(DialogDisplayer.class);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(new JScrollPane(createList()), "OpenIt");
        dialogDescriptor.setOptions(new Object[]{NotifyDescriptor.CANCEL_OPTION});
        dialogDescriptor.setModal(false);
        dialog = d.createDialog(dialogDescriptor);
        dialog.addNotify();
        dialog.setVisible(true);
    }

    static void open(String aFile) {
        Project currentProject = getCurrentProject();
        if (currentProject != null) {
            String projectBaseUrl = currentProject.getProjectDirectory().getPath();
            try {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(projectBaseUrl + File.separator + aFile)));
                DataObject data = DataObject.find(fo);
                //            data.getLookup().lookup(OpenCookie.class).open();
                data.getLookup().lookup(EditCookie.class).edit();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        dialog.setVisible(false);
    }

    JList createList() {
        String config = NbPreferences
                .forModule(OpenItPanel.class)
                .get(OpenItPanel.FILES, "src/main/resources/application.properties");
        final JList list = new JList(config.split("\\n"));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent aKeyEvent) {
                open((String) list.getSelectedValue());
            }

        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aMouseEvent) {
                if (aMouseEvent.getClickCount() == 2 && !aMouseEvent.isConsumed()) {
                    aMouseEvent.consume();
                    open((String) list.getSelectedValue());
                }
            }

        });
        return list;
    }

    static Project getCurrentProject() {
        Lookup lookup = Utilities.actionsGlobalContext();

        for (Project p : lookup.lookupAll(Project.class)) {
            return p;
        }

        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);

            if (p != null) {
                return p;
            }
        }
        for (FileObject fObj : lookup.lookupAll(FileObject.class)) {
            Project p = FileOwnerQuery.getOwner(fObj);

            if (p != null) {
                return p;
            }
        }

        return null;
    }

}
