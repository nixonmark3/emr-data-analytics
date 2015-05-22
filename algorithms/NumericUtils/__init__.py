__author__ = 'BigDataAnalytics'

import numpy as np


def center_normal(df_scale):
    df_mean = np.mean(df_scale, axis=0)
    df_std = np.std(df_scale, axis=0) + 1e-6
    return (df_scale - df_mean) / df_std

