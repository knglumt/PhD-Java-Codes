import math

from Classification.Classifier.Classifier import Classifier
from Classification.InstanceList.InstanceList import InstanceList
from Classification.Model.DecisionTree.DecisionNodeWeighted import DecisionNodeWeighted
from Classification.Model.DecisionTree.DecisionTree import DecisionTree
from Classification.Model.AdaBoostModel import AdaBoostModel
from Classification.Parameter import BaggingParameter


class AdaBoost(Classifier):

    def train(self, trainSet: InstanceList, parameters: BaggingParameter):
        """
        Training algorithm for adaptive boosting classifier. Basically the algorithm creates K distinct stump decision trees,
        In each training iteration, the class weights are updated according to the previous error rate and
        the decision trees are trained sequentially. Predictions of the decision trees are combined according to the class weights.

        PARAMETERS
        ----------
        trainSet : InstanceList
            Training data given to the algorithm
        parameters : BaggingParameter
            Parameters of the adaboost algorithm. ensembleSize returns the number of trees in the algorithm.
        """

        iteration = parameters.getEnsembleSize()
        sampleCount = trainSet.size()
        # initialize arrays
        sampleWeights = []
        y = []
        for j in range(0, sampleCount):
            y.append(0)
        for i in range(0, iteration):
            sampleWeights.append(y)
        trees = []
        for i in range(0, iteration):
            trees.append(DecisionTree)
        treeWeights = []
        for i in range(0, iteration):
            treeWeights.append(0)
        errors = []
        for i in range(0, iteration):
            errors.append(0)
        # initialize class weights
        for i in range(0, sampleCount):
            sampleWeights[0][i] = 1 / sampleCount
        # training loop
        for i in range(iteration):
            # training weak learner with current class weights
            currentSampleWeights = sampleWeights[i]
            # using DecisionTree for weak learner
            tree = DecisionTree(DecisionNodeWeighted(trainSet, currentSampleWeights, None, None, True))
            # calculating errorRate and tree weight (alpha) from weak learner prediction
            error = 0
            predictions = []
            for instance, weight in zip(trainSet.getInstances(), currentSampleWeights):
                prediction = tree.predict(instance)
                error += int((instance.getClassLabel() != prediction)) * weight
                predictions.append(prediction)
            errorRate = error / sampleCount
            treeWeight = math.log((1 - errorRate) / errorRate) / 2
            # updating sample weights
            newSampleWeights = currentSampleWeights
            sumNewSampleWeights = 0
            for j in range(sampleCount):
                newSampleWeights[j] = currentSampleWeights[j] * math.exp(-treeWeight * ((2 * int(trainSet.get(j).getClassLabel() == predictions[j]))-1))
                sumNewSampleWeights += newSampleWeights[j]
            for j in range(sampleCount):
                newSampleWeights[j] /= sumNewSampleWeights
            # updating sample weights for next iteration
            if i + 1 < iteration:
                sampleWeights[i+1] = newSampleWeights
            trees[i] = tree
            treeWeights[i] = treeWeight
            errors[i] = errorRate

        self.model = AdaBoostModel(trees, treeWeights)
