"""
IRT (Item Response Theory) Engine — 3-Parametric Model
Adaptive question selection based on student ability (theta).
"""

import numpy as np
from dataclasses import dataclass
from typing import Optional


@dataclass
class IRTItem:
    id: str
    concept: str
    subject: str
    difficulty: float      # b parameter (-3 to +3)
    discrimination: float  # a parameter (0.5 to 2.5)
    guessing: float = 0.25 # c parameter (0.0 to 0.35)


@dataclass
class IRTUpdateResult:
    old_theta: float
    new_theta: float
    delta: float
    is_correct: bool


class IRTEngine:
    """
    3-Parametric Logistic Model for Adaptive Learning.
    Updates student ability (theta) using Fisher Information gradient.
    """

    LEARNING_RATE = 0.3
    THETA_MIN = -4.0
    THETA_MAX = 4.0

    def probability(self, theta: float, item: IRTItem) -> float:
        """P(correct | theta, item) — 3PL model."""
        z = item.discrimination * (theta - item.difficulty)
        return item.guessing + (1.0 - item.guessing) / (1.0 + np.exp(-z))

    def fisher_information(self, theta: float, item: IRTItem) -> float:
        """Fisher Information: how much this item reveals about theta."""
        p = self.probability(theta, item)
        q = 1.0 - p
        num = item.discrimination**2 * (p - item.guessing)**2 * q
        den = (1.0 - item.guessing)**2 * p + 1e-9
        return num / den

    def update_theta(self, theta: float, item: IRTItem, is_correct: bool) -> IRTUpdateResult:
        """Bayesian theta update: gradient ascent on log-likelihood."""
        p = self.probability(theta, item)
        info = self.fisher_information(theta, item)
        delta = (int(is_correct) - p) / (info + 1e-9)
        new_theta = np.clip(
            theta + self.LEARNING_RATE * delta,
            self.THETA_MIN,
            self.THETA_MAX,
        )
        return IRTUpdateResult(
            old_theta=theta,
            new_theta=float(new_theta),
            delta=float(delta),
            is_correct=is_correct,
        )

    def select_next_item(self, theta: float, item_bank: list[IRTItem]) -> Optional[IRTItem]:
        """Maximum Fisher Information selection (adaptive CAT)."""
        if not item_bank:
            return None
        return max(item_bank, key=lambda item: self.fisher_information(theta, item))

    def mastery_score(self, theta: float) -> float:
        """Convert theta to [0, 1] mastery percentage."""
        return float(1.0 / (1.0 + np.exp(-theta)))


# ---------------------------------------------------------------------------
# Singleton
# ---------------------------------------------------------------------------
irt_engine = IRTEngine()
