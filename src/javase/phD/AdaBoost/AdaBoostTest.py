import unittest

from Classification.Classifier.AdaBoost import AdaBoost
from Classification.Parameter.BaggingParameter import BaggingParameter
from test.Classifier.ClassifierTest import ClassifierTest


class AdaBoostTest(ClassifierTest):

    def test_Train(self):
        adaBoost = AdaBoost()
        adaBoostParameter = BaggingParameter(1, 100)

        adaBoost.train(self.iris.getInstanceList(), adaBoostParameter)
        self.assertAlmostEqual(32.67, 100 * adaBoost.test(self.iris.getInstanceList()).getErrorRate(), 2)
        adaBoost.train(self.bupa.getInstanceList(), adaBoostParameter)
        self.assertAlmostEqual(42.03, 100 * adaBoost.test(self.bupa.getInstanceList()).getErrorRate(), 2)
        adaBoost.train(self.dermatology.getInstanceList(), adaBoostParameter)
        self.assertAlmostEqual(68.31, 100 * adaBoost.test(self.dermatology.getInstanceList()).getErrorRate(), 2)
        adaBoost.train(self.car.getInstanceList(), adaBoostParameter)
        self.assertAlmostEqual(29.98, 100 * adaBoost.test(self.car.getInstanceList()).getErrorRate(), 2)
        adaBoost.train(self.tictactoe.getInstanceList(), adaBoostParameter)
        self.assertAlmostEqual(30.06, 100 * adaBoost.test(self.tictactoe.getInstanceList()).getErrorRate(), 2)
        adaBoost.train(self.nursery.getInstanceList(), adaBoostParameter)
        self.assertAlmostEqual(29.03, 100 * adaBoost.test(self.nursery.getInstanceList()).getErrorRate(), 2)
        adaBoost.train(self.chess.getInstanceList(), adaBoostParameter)
        self.assertAlmostEqual(81.99, 100 * adaBoost.test(self.chess.getInstanceList()).getErrorRate(), 2)

if __name__ == '__main__':
    unittest.main()
