/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.common;


import lombok.SneakyThrows;
import org.apache.directory.shared.ldap.model.filter.*;

public class Filter {

    private final ExprNode node;

    @SneakyThrows
    public Filter(String filter) {
        node = FilterParser.parse(filter);
    }

    public String getAttribute(String name) {
        return getAttribute(node, name);
    }

    private String getAttribute(ExprNode node, String name) {
        if (node instanceof NotNode) {
            return null;
        }
        if (node instanceof OrNode || node instanceof AndNode) {
            BranchNode branch = (BranchNode) node;
            for (ExprNode child : branch.getChildren()) {
                String value = getAttribute(child, name);
                if (value != null) {
                    return value;
                }
            }
        }
        if (node instanceof EqualityNode) {
            @SuppressWarnings("unchecked")
            EqualityNode<String> equalityNode = (EqualityNode<String>) node;
            if (equalityNode.getAttribute().contentEquals(name)) {
                return equalityNode.getValue().getString();
            }
        }
        return null;
    }
}
