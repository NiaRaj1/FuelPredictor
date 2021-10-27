#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Oct 26 15:26:20 2021

@author: nehagarg
"""

import tensorflow as tf
import pandas as pd
from tensorflow import keras
from tensorflow.keras import layers

database_path = keras.utils.get_file("auto-mpg.data", "http://archive.ics.uci.edu/ml/machine-learning-databases/auto-mpg/auto-mpg.data")
database_path

column_names = ['MPG','Cylinders','Displacement','Horsepower','Weight',
                'Acceleration', 'Model Year', 'Origin']

aw_dataset = pd.read_csv(database_path,names = column_names, na_values = "?", 
                     comment='\t',
                      sep=" ", skipinitialspace=True)
newData = aw_dataset.copy()
newData.tail()

newData.isna().sum()
newData = newData.dropna()


origin = newData.pop('Origin')
newData['USA'] = (origin == 1) * 1.0
newData['Europe'] = (origin == 2) * 1.0
newData['Japan'] = (origin == 3) * 1.0


train = newData.sample(frac = 0.8,random_state=0)
test = newData.drop(train.index)

trainLabel = train.pop('MPG')
testLabel = test.pop('MPG')

stats = train.describe()
stats = stats.transpose()


statsTest = test.describe()
statsTest = statsTest.transpose()

def norm(x):
    return (x-stats['mean'])/stats['std']

def normTest(x):
    return (x-statsTest['mean'])/statsTest['std']

normTrain = norm(train)
normTest =  normTest(test)

print(train.keys())

def buildModel():

    model = keras.Sequential([layers.Dense(64, activation= 'relu', input_shape = [len(train.keys())]),
                         layers.Dense(64, activation= 'relu'),
                         layers.Dense(1)])

    optimizer = tf.keras.optimizers.RMSprop(0.001)

    model.compile(loss='mse', optimizer = optimizer, metrics = ['mae','mse'])
    
    return model

model = buildModel()
model.summary()


history = model.fit(
  normTrain, trainLabel,
  epochs=1000)

loss, mae, mse = model.evaluate(normTest, testLabel)

testPredictions = model.predict(normTest).flatten()

fuelLinearModel = 'fuelLinearModel.h5'
tf.keras.models.save_model(model, fuelLinearModel)
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tfModel = converter.convert()
open('automobile.tflite', 'wb').write(tfModel)




