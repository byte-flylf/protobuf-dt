/*
 * Copyright (c) 2012 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.cdt.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;

import com.google.eclipse.protobuf.ui.editor.ModelObjectDefinitionNavigator.Query;
import com.google.inject.Inject;

/**
 * Opens the declaration of a C++ element in the corresponding .proto file.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class OpenProtoDeclarationAction implements IEditorActionDelegate {
  private IEditorPart editor;
  private ITextSelection selection;

  @Inject private ModelObjectDefinitionQueryBuilder queryBuilder;
  @Inject private NavigationJobs navigationJobs;

  @Override public void run(IAction action) {
    if (editor == null || selection == null) {
      return;
    }
    Query query = queryBuilder.buildQuery(editor, selection);
    if (query != null) {
      navigationJobs.scheduleUsing(query);
    }
  }

  @Override public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof ITextSelection) {
      this.selection = (ITextSelection) selection;
      return;
    }
    this.selection = null;
  }

  @Override public void setActiveEditor(IAction action, IEditorPart editor) {
    this.editor = editor;
  }
}
