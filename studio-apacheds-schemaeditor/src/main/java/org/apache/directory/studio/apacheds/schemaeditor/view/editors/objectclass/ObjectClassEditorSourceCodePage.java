/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.studio.apacheds.schemaeditor.view.editors.objectclass;


import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.directory.server.core.tools.schema.ObjectClassLiteral;
import org.apache.directory.server.core.tools.schema.OpenLdapSchemaParser;
import org.apache.directory.studio.apacheds.schemaeditor.model.ObjectClassImpl;
import org.apache.directory.studio.apacheds.schemaeditor.model.openldapfile.OpenLdapSchemaExporter;
import org.apache.directory.studio.apacheds.schemaeditor.view.widget.SchemaSourceViewer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;


/**
 * This class is the Source Code Page of the Object Class Editor
 */
public class ObjectClassEditorSourceCodePage extends FormPage
{
    /** The page ID */
    public static final String ID = ObjectClassEditor.ID + "sourceCodePage"; //$NON-NLS-1$

    /** The page title*/
    public static final String TITLE = "Source Code"; //$NON-NLS-1$

    /** The modified object class */
    private ObjectClassImpl modifiedObjectClass;

    /** The Schema Source Viewer */
    private SchemaSourceViewer schemaSourceViewer;

    /** The flag to indicate if the user can leave the Source Code page */
    private boolean canLeaveThePage = true;

    /** The listener of the Schema Source Viewer Widget */
    private ModifyListener schemaSourceViewerListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            canLeaveThePage = true;
            try
            {
                ( ( ObjectClassEditor ) getEditor() ).setDirty( true );
                OpenLdapSchemaParser parser = new OpenLdapSchemaParser();
                parser.parse( schemaSourceViewer.getTextWidget().getText() );

                List objectclasses = parser.getObjectClassTypes();
                if ( objectclasses.size() != 1 )
                {
                    // TODO Throw an exception and return
                }
                else
                {
                    updateObjectClass( ( ObjectClassLiteral ) objectclasses.get( 0 ) );
                }
            }
            catch ( IOException e1 )
            {
                canLeaveThePage = false;
            }
            catch ( ParseException exception )
            {
                canLeaveThePage = false;
            }
        }
    };


    /**
     * Default constructor
     * @param editor
     *      the associated editor
     */
    public ObjectClassEditorSourceCodePage( FormEditor editor )
    {
        super( editor, ID, TITLE );
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        form.getBody().setLayout( layout );
        toolkit.paintBordersFor( form.getBody() );

        modifiedObjectClass = ( ( ObjectClassEditor ) getEditor() ).getModifiedObjectClass();

        // SOURCE CODE Field
        schemaSourceViewer = new SchemaSourceViewer( form.getBody(), null, null, false, SWT.BORDER | SWT.H_SCROLL
            | SWT.V_SCROLL );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        gd.heightHint = 10;
        schemaSourceViewer.getTextWidget().setLayoutData( gd );

        // set text font
        Font font = JFaceResources.getFont( JFaceResources.TEXT_FONT );
        schemaSourceViewer.getTextWidget().setFont( font );

        IDocument document = new Document();
        schemaSourceViewer.setDocument( document );

        // Initialization from the "input" object class
        fillInUiFields();

        schemaSourceViewer.getTextWidget().addModifyListener( schemaSourceViewerListener );
    }


    /**
     * Fills in the User Interface.
     */
    private void fillInUiFields()
    {
        // SOURCE CODE Field
        schemaSourceViewer.getDocument().set( OpenLdapSchemaExporter.toSourceCode( modifiedObjectClass ) );
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormPage#canLeaveThePage()
     */
    public boolean canLeaveThePage()
    {
        return canLeaveThePage;
    }


    /**
     * Updates the Modified Object Class from the given Object Class Literal.
     *
     * @param ocl
     *      the Object Class Literal
     */
    private void updateObjectClass( ObjectClassLiteral ocl )
    {
        modifiedObjectClass.setDescription( ocl.getDescription() );
        modifiedObjectClass.setMayNamesList( ocl.getMay() );
        modifiedObjectClass.setMustNamesList( ocl.getMust() );
        modifiedObjectClass.setNames( ocl.getNames() );
        modifiedObjectClass.setObsolete( ocl.isObsolete() );
        modifiedObjectClass.setOid( ocl.getOid() );
        modifiedObjectClass.setSuperClassesNames( ocl.getSuperiors() );
        modifiedObjectClass.setType( ocl.getClassType() );
    }


    /**
     * Refreshes the UI.
     */
    public void refreshUI()
    {
        fillInUiFields();
    }
}
