from Math.DiscreteDistribution import DiscreteDistribution
from itertools import groupby

from Classification.Instance.Instance import Instance
from Classification.Model.Model import Model


class AdaBoostModel(Model):

    __trees = None
    __treeWeights = None
    __errors = None
    __sampleWeights = None
    __adaErrors = None

    def __init__(self, trees: list, treeWeights: list):
        """
        A constructor which sets the default values of the AdaBoost Classifier.

        PARAMETERS
        ----------
        trees list
            A list of DecisionTrees.
        treeWeights list
            Weights of DecisionTrees.
        """

        self.__trees = trees
        self.__treeWeights = treeWeights

    def predict(self, instance: Instance) -> str:
        """
        The predict method takes an Instance as an input and loops through the list of DecisionTrees.
        Returns the prediction with the highest total class weight by summing the class weights of the predictions for each tree

        PARAMETERS
        ----------
        instance : Instance
            Instance to make prediction.

        RETURNS
        -------
        str
            The prediction with the highest total class weight
        """
        predictions = []
        for tree, weight in zip(self.__trees, self.__treeWeights):
            prediction = tree.predict(instance)
            predictions.append([prediction, weight])

        groupPredictions = []
        for i, g in groupby(sorted(predictions), key=lambda x: x[0]):
            groupPredictions.append([i, sum(v[1] for v in g)])

        maxWeight = 0
        maxPrediction = ''
        for prediction, weight in groupPredictions:
            if weight > maxWeight:
                maxPrediction = prediction

        return maxPrediction

    def predictProbability(self, instance: Instance) -> dict:
        # TODO: update probability calculation
        distribution = DiscreteDistribution()
        for tree, weight in zip(self.__trees, self.__treeWeights):
            for i in range(round(100 * weight)):
                distribution.addItem(tree.predict(instance))
        return distribution.getProbabilityDistribution()
