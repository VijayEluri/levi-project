package org.levi.engine.impl.bpmn;

import org.omg.spec.bpmn.x20100524.model.TParallelGateway;
import org.omg.spec.bpmn.x20100524.model.TSequenceFlow;

import java.util.ArrayList;
import java.util.List;

public final class ParallelGateway extends Gateway {
    private final List<TSequenceFlow> output;

    public ParallelGateway(TParallelGateway gateway, FlowNodeFactory factory) {
        super(gateway, factory);
        output = new ArrayList<TSequenceFlow>(super.outgoingSeqFlowSet.size());
    }

    public List<TSequenceFlow> evaluate() {
        System.out.println("<Parallel Gateway " + getName() + " Evaluating>");
        output.clear();
        if (compare()) {
<<<<<<< HEAD
            for (TSequenceFlow sf : outgoingSeqFlowSet) {
                output.add(sf);
            }
            /*
            for (int i = outgoingSeqFlowSet.size()-1; i >= 0; --i) {
                output.add(outgoingSeqFlowSet.get(i));
            }*/
        } else {
            //System.out.println(" not done...");
=======
        //    for (int i = outgoingSeqFlowSet.size()-1; i >= 0; --i) {
        //        output.add(outgoingSeqFlowSet.get(i));
        //    }
        //} else {
        //    //System.out.println(" not done...");
        //}

            if(isDiverging() || (incomingSeqFlowSet.size()==1)) {
                for(int i=0;i< outgoingSeqFlowSet.size();i++){
                    System.out.println(">>>>>=============>>>>>");
                    output.add(outgoingSeqFlowSet.get(i));
                }
            } else if(isConverging() || (outgoingSeqFlowSet.size()==1)) {
                System.out.println("<<<<<=============<<<<<");
                if(incomingSeqFlowSet.size() == 1){
                    output.add(outgoingSeqFlowSet.get(0));
                } else {
                    if(incomingTokens.size() == incomingSeqFlowSet.size()) {
                        output.add(outgoingSeqFlowSet.get(0));
                    }
                }

            } else {
                System.out.println(" not done...");
            }
>>>>>>> 5c28ef3a90c97cca86f6880eb6a240539ef81be0
        }

        return output;
    }

    private boolean compare() {
        boolean result = false;
        if (incomingTokens.size() == incomingSeqFlowSet.size()) {
            for (TSequenceFlow sf : incomingSeqFlowSet) {
                for (String token : incomingTokens) {
                    if (token.equals(sf.getId())) {
                        result = true;
                        break;
                    }
                    result = false;
                }
                if (!result) {
                    throw new IllegalArgumentException("incoming sequence flows unmatched");
                }
            }
        } else {
            //System.err.println("<Parallel Gateway Still waiting for other tokens>");
        }
        if (result) {
            incomingTokens.clear();
        }
        return result;
    }

    @Override public String toString() {
        String out = "{";
        out += "ParallelGateway: " + getId() + "}";
        return out;
    }
}
