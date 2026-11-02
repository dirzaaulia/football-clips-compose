package com.dirzaaulia.footballclips.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub

@Composable
fun OnboardingScreen(
    clubs: List<SupportedClub>,
    initialDarkMode: Boolean = true,
    onComplete: (isSpoilerFree: Boolean, isDarkMode: Boolean, selectedClubs: Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var isSpoilerFree by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(initialDarkMode) }
    var selectedClubs by remember { mutableStateOf(emptySet<String>()) }

    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        topBar = {
            OnboardingTopBar(
                canSkip = step != OnboardingStep.PREFERENCES,
                onSkip = { onComplete(isSpoilerFree, isDarkMode, selectedClubs) },
            )
        },
        bottomBar = {
            OnboardingBottomNav(
                step = step,
                onBack = {
                    step = when (step) {
                        OnboardingStep.PREFERENCES -> OnboardingStep.SPOILER_MODE
                        OnboardingStep.SPOILER_MODE -> OnboardingStep.WELCOME
                        OnboardingStep.WELCOME -> OnboardingStep.WELCOME
                    }
                },
                onNext = {
                    when (step) {
                        OnboardingStep.WELCOME -> step = OnboardingStep.SPOILER_MODE
                        OnboardingStep.SPOILER_MODE -> step = OnboardingStep.PREFERENCES
                        OnboardingStep.PREFERENCES -> onComplete(isSpoilerFree, isDarkMode, selectedClubs)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            StepProgressDots(currentStep = step)
            Spacer(modifier = Modifier.height(24.dp))
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep()
                OnboardingStep.SPOILER_MODE -> SpoilerModeStep(
                    isSpoilerFree = isSpoilerFree,
                    onSelectMode = { isSpoilerFree = it },
                )
                OnboardingStep.PREFERENCES -> PreferencesStep(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = it },
                    selectedClubs = selectedClubs,
                    onToggleClub = { clubId ->
                        selectedClubs = if (clubId in selectedClubs) selectedClubs - clubId else selectedClubs + clubId
                    },
                    clubs = clubs,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingTopBar(canSkip: Boolean, onSkip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        if (canSkip) {
            TextButton(onClick = onSkip) {
                Text("Skip", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StepProgressDots(currentStep: OnboardingStep) {
    val steps = OnboardingStep.entries
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEach { s ->
            val isActive = s == currentStep
            val isPassed = s.ordinal < currentStep.ordinal
            val color = when {
                isActive -> MaterialTheme.colorScheme.primary
                isPassed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val width = if (isActive) 24.dp else 8.dp
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun OnboardingBottomNav(
    step: OnboardingStep,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (step != OnboardingStep.WELCOME) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.size(1.dp))
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = if (step == OnboardingStep.PREFERENCES) "Get Started" else "Next",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
