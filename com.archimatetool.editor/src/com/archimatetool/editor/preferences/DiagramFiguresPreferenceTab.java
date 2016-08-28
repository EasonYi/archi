/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.editor.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.archimatetool.editor.ui.ArchimateLabelProvider;
import com.archimatetool.editor.ui.FigureChooser;
import com.archimatetool.model.IArchimatePackage;


/**
 * Default Figures Preferences Tab panel
 * 
 * @author Phillip Beauvoir
 */
public class DiagramFiguresPreferenceTab implements IPreferenceConstants {
    
    private List<ImageChoice> fChoices = new ArrayList<ImageChoice>();
    
    private TableViewer fTableViewer;
    
    private class ImageChoice {
        String name;
        String preferenceKey;
        int chosenType = 0;
        Image[] images;
        
        ImageChoice(EClass eClass) {
            name = ArchimateLabelProvider.INSTANCE.getDefaultName(eClass);
            this.preferenceKey = FigureChooser.getDefaultFigurePreferenceKeyForClass(eClass);
            images = FigureChooser.getFigurePreviewImagesForClass(eClass);
            chosenType = Preferences.STORE.getInt(preferenceKey);
        }
        
        Image getImage() {
            return images[chosenType];
        }
        
        void toggle() {
            chosenType = (chosenType == 0) ? 1 : 0;
        }
    }
    
    public Composite createContents(Composite parent) {
        loadFigures();
        
        Composite client = new Composite(parent, SWT.NULL);
        client.setLayout(new GridLayout());
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        
        Composite tableClient = new Composite(client, SWT.NULL);
        tableClient.setLayout(gridLayout);
        tableClient.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Label label = new Label(tableClient, SWT.NULL);
        label.setText(Messages.DiagramFiguresPreferencePage_0);
        
        Composite client2 = new Composite(tableClient, SWT.NULL);
        client2.setLayout(new TableColumnLayout());
        
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 80; // need this to set a smaller height
        gd.widthHint = 80;  // need this to stop it getting larger when the splitter is resized in the Prefs dialog
        client2.setLayoutData(gd);
        
        createTable(client2);
        
        fTableViewer.setInput(fChoices);
        
        return client;
    }
    
    private void loadFigures() {
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getBusinessProcess()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getBusinessInterface()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getBusinessService()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getApplicationComponent()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getApplicationInterface()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getApplicationService()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getDevice()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getNode()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getTechnologyInterface()));
        fChoices.add(new ImageChoice(IArchimatePackage.eINSTANCE.getTechnologyService()));
    }
    
    private void createTable(Composite parent) {
        fTableViewer = new TableViewer(parent, SWT.BORDER);
        
        TableColumnLayout layout = (TableColumnLayout)parent.getLayout();
        TableViewerColumn column = new TableViewerColumn(fTableViewer, SWT.NONE);
        layout.setColumnData(column.getColumn(), new ColumnWeightData(100, false));
        
        // Fix row height
        // This is definitely needed on some Linux versions where the row height is stuck at 17 for some reason
        fTableViewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(Event event) {
                event.height = 72;
             }
        });
        
        fTableViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return ((List<?>)inputElement).toArray();
            }
        });
        
        class TableLabelProvider extends LabelProvider {
            @Override
            public Image getImage(Object element) {
                return ((ImageChoice)element).getImage();
            }

            @Override
            public String getText(Object element) {
                ImageChoice choice = (ImageChoice)element;
                
                return "   " + //$NON-NLS-1$
                        choice.name +
                        " "  + //$NON-NLS-1$
                        (choice.chosenType + 1);
            }
        }
        
        fTableViewer.setLabelProvider(new TableLabelProvider());
        
        fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ImageChoice ic = (ImageChoice)((IStructuredSelection)event.getSelection()).getFirstElement();
                if(ic != null) {
                    ic.toggle();
                    fTableViewer.update(ic, null);
                }
            }
        });
    }
    
    protected boolean performOk() {
        for(ImageChoice choice : fChoices) {
            Preferences.STORE.setValue(choice.preferenceKey, choice.chosenType);
        }

        return true;
    }
    
    protected void performDefaults() {
        for(ImageChoice choice : fChoices) {
            choice.chosenType = 0;
        }
        
        fTableViewer.refresh();
    }
}