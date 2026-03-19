package com.goatce.ot;

import org.springframework.stereotype.Service;

@Service
public class OperationalTransformService {

    /**
     * Transforms op2 against op1 (op1 happened first).
     * @param op1 The operation that occurred first
     * @param op2 The operation to transform
     * @return Transformed operation, or null if it becomes a no-op
     */
    public OperationDTO transform(OperationDTO op1, OperationDTO op2) {
        String type1 = op1.getType();
        String type2 = op2.getType();
        int pos1 = op1.getPosition();
        int pos2 = op2.getPosition();

        if ("INSERT".equals(type1) && "INSERT".equals(type2)) {
            if (pos1 <= pos2) {
                op2.setPosition(pos2 + 1);
            }
        } else if ("INSERT".equals(type1) && "DELETE".equals(type2)) {
            if (pos1 <= pos2) {
                op2.setPosition(pos2 + 1);
            }
        } else if ("DELETE".equals(type1) && "INSERT".equals(type2)) {
            if (pos1 < pos2) {
                op2.setPosition(pos2 - 1);
            }
        } else if ("DELETE".equals(type1) && "DELETE".equals(type2)) {
            if (pos1 < pos2) {
                op2.setPosition(pos2 - 1);
            } else if (pos1 == pos2) {
                return null;
            }
        }

        return op2;
    }

    /**
     * Applies an operation to a document string.
     * @param document The original document text
     * @param op The operation to apply
     * @return The updated document text
     */
    public String applyOperation(String document, OperationDTO op) {
        if (document == null) {
            document = "";
        }

        if ("INSERT".equals(op.getType())) {
            if (op.getPosition() >= document.length()) {
                return document + op.getCharacter();
            } else {
                return document.substring(0, op.getPosition())
                        + op.getCharacter()
                        + document.substring(op.getPosition());
            }
        } else if ("DELETE".equals(op.getType())) {
            if (op.getPosition() >= document.length() || document.isEmpty()) {
                return document;
            } else {
                return document.substring(0, op.getPosition())
                        + document.substring(op.getPosition() + 1);
            }
        }

        return document;
    }
}
