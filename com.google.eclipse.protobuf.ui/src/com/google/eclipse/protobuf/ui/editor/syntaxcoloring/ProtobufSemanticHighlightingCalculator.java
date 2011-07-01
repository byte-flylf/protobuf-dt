/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.ui.editor.syntaxcoloring;

import static com.google.eclipse.protobuf.protobuf.ProtobufPackage.Literals.*;
import static com.google.eclipse.protobuf.ui.editor.syntaxcoloring.HighlightingConfiguration.*;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.syntaxcoloring.*;

import com.google.eclipse.protobuf.protobuf.*;
import com.google.eclipse.protobuf.protobuf.Enum;
import com.google.eclipse.protobuf.protobuf.Package;
import com.google.eclipse.protobuf.util.*;
import com.google.inject.Inject;

/**
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ProtobufSemanticHighlightingCalculator implements ISemanticHighlightingCalculator {

  @Inject private ModelNodes nodes;
  @Inject private FieldOptions fieldOptions;

  public void provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor) {
    if (resource == null) return;
    EList<EObject> contents = resource.getContents();
    if (contents == null || contents.isEmpty()) return;
    Protobuf protobuf = (Protobuf) contents.get(0);
    highlight(protobuf, acceptor);
  }

  private void highlight(Protobuf protobuf, IHighlightedPositionAcceptor acceptor) {
    for (ProtobufElement element : protobuf.getElements()) {
      if (element instanceof Package) {
        highlightName(element, acceptor, DEFAULT_ID);
        continue;
      }
      if (element instanceof Option) {
        highlight((Option) element, acceptor);
        continue;
      }
      if (element instanceof Type) {
        highlight((Type) element, acceptor);
        continue;
      }
      if (element instanceof ExtendMessage) {
        highlight((ExtendMessage) element, acceptor);
        continue;
      }
      if (element instanceof Service) {
        highlight((Service) element, acceptor);
      }
    }
  }

  private void highlight(ExtendMessage extend, IHighlightedPositionAcceptor acceptor) {
    highlightFirstFeature(extend, EXTEND_MESSAGE__MESSAGE, acceptor, MESSAGE_ID);
    for (MessageElement element : extend.getElements()) {
      highlight(element, acceptor);
    }
  }

  private void highlight(Type type, IHighlightedPositionAcceptor acceptor) {
    if (type instanceof Message) {
      highlight((Message) type, acceptor);
      return;
    }
    if (type instanceof Enum) {
      highlight((Enum) type, acceptor);
    }
  }

  private void highlight(Message message, IHighlightedPositionAcceptor acceptor) {
    highlightName(message, acceptor, MESSAGE_DEFINITION_ID);
    for (MessageElement element : message.getElements()) {
      highlight(element, acceptor);
    }
  }

  private void highlight(MessageElement element, IHighlightedPositionAcceptor acceptor) {
    if (element instanceof Option) {
      highlight((Option) element, acceptor);
      return;
    }
    if (element instanceof Field) {
      highlight((Field) element, acceptor);
      return;
    }
    if (element instanceof Type) {
      highlight((Type) element, acceptor);
      return;
    }
    if (element instanceof ExtendMessage) {
      highlight((ExtendMessage) element, acceptor);
    }
  }

  private void highlight(Field field, IHighlightedPositionAcceptor acceptor) {
    highlightName(field, acceptor, DEFAULT_ID);
    highlightFirstFeature(field, FIELD__INDEX, acceptor, MESSAGE_FIELD_INDEX_ID);
    highlightOptions(field, acceptor);
    if (field instanceof Group) {
      highlight((Group) field, acceptor);
      return;
    }
    if (field instanceof Property) {
      highlight((Property) field, acceptor);
    }
  }

  private void highlightOptions(Field field, IHighlightedPositionAcceptor acceptor) {
    for (FieldOption option : field.getFieldOptions()) {
      String highlightId = fieldOptions.isDefaultValueOption(option) ? KEYWORD_ID : DEFAULT_ID;
      highlightName(option, acceptor, highlightId);
      ValueRef ref = option.getValue();
      if (ref instanceof LiteralRef) {
        highlightFirstFeature(option, FIELD_OPTION__VALUE, acceptor, ENUM_LITERAL_ID);
        return;
      }
      if (ref instanceof NumberRef) {
        highlightFirstFeature(option, FIELD_OPTION__VALUE, acceptor, NUMBER_ID);
      }
    }
  }

  private void highlight(Group group, IHighlightedPositionAcceptor acceptor) {
    for (GroupElement e : group.getElements()) {
      if (e instanceof Field) {
        highlight((Field) e, acceptor);
        continue;
      }
      if (e instanceof Enum) {
        highlight((Enum) e, acceptor);
      }
    }
  }

  private void highlight(Property property, IHighlightedPositionAcceptor acceptor) {
    highlightPropertyType(property, acceptor);
  }

  private void highlightPropertyType(Property property, IHighlightedPositionAcceptor acceptor) {
    AbstractTypeReference ref = property.getType();
    if (!(ref instanceof TypeReference)) return;
    Type type = ((TypeReference) ref).getType();
    if (type instanceof Message) {
      highlightFirstFeature(property, PROPERTY__TYPE, acceptor, MESSAGE_ID);
      return;
    }
    if (type instanceof Enum) {
      highlightFirstFeature(property, PROPERTY__TYPE, acceptor, ENUM_ID);
      return;
    }
  }

  private void highlight(Enum anEnum, IHighlightedPositionAcceptor acceptor) {
    highlightName(anEnum, acceptor, ENUM_DEFINITION_ID);
    for (EnumElement element : anEnum.getElements()) {
      if (element instanceof Literal) {
        highLight((Literal) element, acceptor);
        continue;
      }
      if (element instanceof Option) {
        highlight((Option) element, acceptor);
      }
    }
  }

  private void highLight(Literal literal, IHighlightedPositionAcceptor acceptor) {
    highlightName(literal, acceptor, ENUM_LITERAL_DEFINITION);
    highlightFirstFeature(literal, LITERAL__INDEX, acceptor, ENUM_LITERAL_INDEX_ID);
  }

  private void highlight(Service service, IHighlightedPositionAcceptor acceptor) {
    highlightName(service, acceptor, SERVICE_DEFINITION_ID);
    for (Rpc rpc : service.getRpcs()) {
      highlight(rpc, acceptor);
    }
  }

  private void highlight(Rpc rpc, IHighlightedPositionAcceptor acceptor) {
    highlightName(rpc, acceptor, RPC_DEFINITION_ID);
    highlightFirstFeature(rpc, RPC__ARG_TYPE, acceptor, RPC_ARGUMENT_ID);
    highlightFirstFeature(rpc, RPC__RETURN_TYPE, acceptor, RPC_RETURN_TYPE_ID);
    for (Option option : rpc.getOptions()) {
      highlight(option, acceptor);
    }
  }

  private void highlight(Option option, IHighlightedPositionAcceptor acceptor) {
    highlightName(option, acceptor, DEFAULT_ID);
    ValueRef ref = option.getValue();
    if (ref instanceof LiteralRef) {
      highlightFirstFeature(option, OPTION__VALUE, acceptor, ENUM_LITERAL_ID);
      return;
    }
    if (ref instanceof NumberRef) {
      highlightFirstFeature(option, OPTION__VALUE, acceptor, NUMBER_ID);
    }
  }

  private void highlightName(EObject o, IHighlightedPositionAcceptor acceptor, String highlightId) {
    highlightFirstFeature(o, o.eClass().getEStructuralFeature("name"), acceptor, highlightId);
  }

  private void highlightFirstFeature(EObject semantic, EStructuralFeature feature,
      IHighlightedPositionAcceptor acceptor, String highlightId) {
    INode node = nodes.firstNodeForFeature(semantic, feature);
    if (node == null || node.getText() == null) return;
    try {
      acceptor.addPosition(node.getOffset(), node.getText().trim().length(), highlightId);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}