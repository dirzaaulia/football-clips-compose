package com.dirzaaulia.footballclips.ui.onboarding

enum class OnboardingStep {
    WELCOME,
    SPOILER_MODE,
    PREFERENCES,
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val isSpoilerFree: Boolean = false,
    val isDarkMode: Boolean = true,
    val selectedClubs: Set<String> = emptySet(),
)
