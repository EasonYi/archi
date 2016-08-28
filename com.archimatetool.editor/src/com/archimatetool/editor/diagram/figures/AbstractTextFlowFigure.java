/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.editor.diagram.figures;

import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.BlockFlow;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;

import com.archimatetool.editor.diagram.util.AnimationUtil;
import com.archimatetool.editor.preferences.IPreferenceConstants;
import com.archimatetool.editor.preferences.Preferences;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.ITextAlignment;



/**
 * Abstract Figure with Text Flow Control
 * 
 * @author Phillip Beauvoir
 */
public abstract class AbstractTextFlowFigure extends AbstractContainerFigure {
    
    private TextFlow fTextFlow;
    
    protected int TEXT_PADDING = 10;
    
    public AbstractTextFlowFigure(IDiagramModelObject diagramModelObject) {
        super(diagramModelObject);
    }
    
    @Override
    protected void setUI() {
        setLayoutManager(new DelegatingLayout());
        
        Locator textLocator = new Locator() {
            public void relocate(IFigure target) {
                Rectangle bounds = calculateTextControlBounds();
                if(bounds != null) {
                    translateFromParent(bounds);
                    target.setBounds(bounds);
                }
            }
        };

        FlowPage page = new FlowPage();
        BlockFlow block = new BlockFlow();
        fTextFlow = new TextFlow();
        
        int wordWrapStyle = Preferences.STORE.getInt(IPreferenceConstants.ARCHIMATE_FIGURE_WORD_WRAP_STYLE);
        fTextFlow.setLayoutManager(new ParagraphTextLayout(fTextFlow, wordWrapStyle));
        
        block.add(fTextFlow);
        page.add(block);
        add(page, textLocator);
        
        Locator mainLocator = new Locator() {
            public void relocate(IFigure target) {
                Rectangle bounds = getBounds().getCopy();
                translateFromParent(bounds);
                target.setBounds(bounds);
            }
        };
        
        add(getMainFigure(), mainLocator);

        // Have to add this if we want Animation to work on figures!
        AnimationUtil.addFigureForAnimation(getMainFigure());
    }
    
    public void refreshVisuals() {
        // Text
        setText();
        
        // Font
        setFont();
        
        // Fill Color
        setFillColor();
        
        // Font Color
        setFontColor();
        
        // Line Color
        setLineColor();
        
        // Alignment default is CENTER
        int alignment = getDiagramModelObject().getTextAlignment();
        if(alignment == ITextAlignment.TEXT_ALIGNMENT_NONE) {
            alignment = ITextAlignment.TEXT_ALIGNMENT_CENTER;
        }
        ((BlockFlow)getTextControl().getParent()).setHorizontalAligment(alignment);
    }
    
    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        
        getTextControl().setEnabled(value);
        
        if(getFigureDelegate() != null) {
            getFigureDelegate().setEnabled(value);
        }
    }
    
    protected void setText() {
        String text = StringUtils.safeString(getDiagramModelObject().getName());
        getTextControl().setText(StringUtils.safeString(text));
    }
    
    public TextFlow getTextControl() {
        return fTextFlow;
    }

    /**
     * Calculate the Text Contrl Bounds or null if none.
     * The Default is to delegate to the Figure Delegate.
     */
    protected Rectangle calculateTextControlBounds() {
        if(getFigureDelegate() != null) {
            return getFigureDelegate().calculateTextControlBounds();
        }
        return null;
    }
}