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

package org.apache.directory.ldapstudio.browser.ui.widgets.entryeditor;


import org.apache.directory.ldapstudio.browser.core.model.IValue;
import org.apache.directory.ldapstudio.browser.core.model.ModelModificationException;
import org.apache.directory.ldapstudio.browser.core.model.schema.SchemaUtils;
import org.apache.directory.ldapstudio.browser.ui.valueeditors.internal.ValueEditorManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;


/**
 * The EntryEditorWidgetCellModifier implements the {@link ICellModifier} interface
 * for the entry editor widget.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class EntryEditorWidgetCellModifier implements ICellModifier
{

    /** The value editor manager. */
    private ValueEditorManager valueEditorManager;


    /**
     * Creates a new instance of EntryEditorWidgetCellModifier.
     *
     * @param valueEditorManager
     */
    public EntryEditorWidgetCellModifier( ValueEditorManager valueEditorManager )
    {
        this.valueEditorManager = valueEditorManager;
    }


    /**
     * Disposes this object.
     */
    public void dispose()
    {
        valueEditorManager = null;
    }


    /**
     * {@inheritDoc}
     */
    public boolean canModify( Object element, String property )
    {
        if ( element != null && element instanceof IValue && valueEditorManager != null )
        {
            IValue attributeValue = ( IValue ) element;

            if ( !SchemaUtils.isModifyable( attributeValue.getAttribute().getAttributeTypeDescription() ) )
            {
                return false;
            }
            if ( attributeValue.isRdnPart() )
            {
                return false;
            }
            if ( EntryEditorWidgetTableMetadata.KEY_COLUMN_NAME.equals( property ) )
            {
                return false;
            }
            if ( EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME.equals( property ) )
            {
                return this.valueEditorManager.getCurrentValueEditor( attributeValue ).getRawValue( attributeValue ) != null;
            }
        }

        return false;
    }


    /**
     * {@inheritDoc}
     */
    public Object getValue( Object element, String property )
    {
        if ( element != null && element instanceof IValue && valueEditorManager != null )
        {
            IValue attributeValue = ( IValue ) element;
            Object returnValue;
            if ( EntryEditorWidgetTableMetadata.KEY_COLUMN_NAME.equals( property ) )
            {
                returnValue = attributeValue.getAttribute().getDescription();
            }
            else if ( EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME.equals( property ) )
            {
                returnValue = this.valueEditorManager.getCurrentValueEditor( attributeValue ).getRawValue(
                    attributeValue );
            }
            else
            {
                returnValue = "";
            }
            return returnValue;
        }
        else
        {
            return null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * TODO: Remove value modification from value editors 
     */
    public void modify( Object element, String property, Object newRawValue )
    {
        if ( element != null && element instanceof Item )
        {
            element = ( ( Item ) element ).getData();
        }

        if ( element != null && element instanceof IValue && valueEditorManager != null )
        {
            IValue attributeValue = ( IValue ) element;

            if ( EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME.equals( property ) )
            {
                try
                {
                    this.valueEditorManager.getCurrentValueEditor( attributeValue ).modifyValue( attributeValue,
                        newRawValue );
                }
                catch ( ModelModificationException mme )
                {
                    MessageDialog.openError( Display.getDefault().getActiveShell(), "Error While Modifying Value", mme
                        .getMessage() );
                }
            }
        }
    }

}
